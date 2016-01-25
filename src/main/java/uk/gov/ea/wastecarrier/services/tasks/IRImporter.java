package uk.gov.ea.wastecarrier.services.tasks;

import com.yammer.dropwizard.tasks.Task;

import java.io.*;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.mongodb.DB;
import net.vz.mongodb.jackson.JacksonDBCollection;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.ImmutableMultimap;
import org.joda.time.DateTime;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.IRConfiguration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.core.Address;
import uk.gov.ea.wastecarrier.services.core.KeyPerson;
import uk.gov.ea.wastecarrier.services.core.MetaData;

/**
 * Imports registration records from CSV files.  It is intended to be used only
 * once, to migrate Waste Carrier Registration data from IR into the new digital
 * service.
 *
 * curl -X POST http://localhost:9091/tasks/ir-import
 *
 * There are no Query String parameters.
 */
public class IRImporter extends Task
{
    // Private static members.
    private static final Logger log = Logger.getLogger(IRRenewalPopulator.class.getName());
    private static final SecureRandom accessCodeRnd = new SecureRandom();
    
    // Private constants and enumerations.
    private static final int ACCESS_CODE_LENGTH = 6;
    private static final String ACCESS_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int ACCESS_CODE_CHARS_LENGTH = ACCESS_CODE_CHARS.length();
    private static final String SOLE_TRADER = "soleTrader";
    private static final String COMPANY = "limitedCompany";
    private static final String PARTNERSHIP = "partnership";
    private static final String PUBLIC_BODY = "publicBody";
    
    private enum CsvColumn {
        Tier             (0),
        CarrierType      (1),
        BusinessType     (2),
        BusinessName     (3),
        CompanyNo        (4),
        ApplicantName    (5),
        PhoneNumber      (6),
        ContactEmail     (7),
        RegAddrBuilding  (10),
        RegAddrLine1     (11),
        RegAddrLine2     (12),
        RegAddrLine3     (13),
        RegAddrLine4     (14),
        RegAddrTown      (15),
        RegAddrPostcode  (16),
        RegAddrEasting   (19),
        RegAddrNorthing  (20),
        PostAddrBuilding (22),
        PostAddrLine1    (23),
        PostAddrLine2    (24),
        PostAddrLine3    (25),
        PostAddrLine4    (26),
        PostAddrTown     (27),
        PostAddrPostcode (28),
        PostAddrName     (30),
        Firstname        (31),
        Lastname         (32),
        DateOfBirth      (33),
        Position         (34),
        PersonKey        (35),
        RegID            (36),
        ExpiryDate       (37),
        RegisteredDate   (38),
        Status           (39);
                
        private final int value;

        private CsvColumn(int value) {
          this.value = value;
        }

        public int index() {
          return value;
        }
    }
        
    // Private instance members set at construction.
    private final DatabaseConfiguration dbConfig;
    private final String irCompanyDataFilePath;
    private final String irIndividualDataFilePath;
    private final String irPartnersDataFilePath;
    private final String irPublicBodyDataFilePath;
    private final SimpleDateFormat dateParser;

    /**
     * Constructor.  Basic initialisation only.
     * @param name The name by which the Task will be known.
     * @param databaseConfig Object containing database configuration.
     * @param irMigrationConfig Object defining where the source data is stored.
     */
    public IRImporter(String name, DatabaseConfiguration databaseConfig, IRConfiguration irMigrationConfig)
    {
        super(name);
        
        // Store database configuration for later use.
        dbConfig = databaseConfig;
        
        // Build fully-qualified paths for the IR migration data sources.
        this.irCompanyDataFilePath = irMigrationConfig.getIrFolderPath()
                + File.separatorChar
                + irMigrationConfig.getIrCompanyFileName();
        
        this.irIndividualDataFilePath = irMigrationConfig.getIrFolderPath()
                + File.separatorChar
                + irMigrationConfig.getIrIndividualFileName();
        
        this.irPartnersDataFilePath = irMigrationConfig.getIrFolderPath()
                + File.separatorChar
                + irMigrationConfig.getIrPartnersFileName();
        
        this.irPublicBodyDataFilePath = irMigrationConfig.getIrFolderPath()
                + File.separatorChar
                + irMigrationConfig.getIrPublicBodyFileName();
        
        // Create a date parser.
        this.dateParser = new SimpleDateFormat("dd/MM/yyyy");
    }
    
    /**
     * Executes the task.
     * @param arg0 Task parameters provided via the query string.  We don't use
     * this value.
     * @param out An object allowing formatted messages to be returned to the
     * REST call originator.
     * @throws Exception 
     */
    @Override
    public void execute(ImmutableMultimap<String, String> arg0, PrintWriter out) throws Exception
    {
        // Build a database helper.
        DatabaseHelper dbHelper = new DatabaseHelper(new DatabaseConfiguration(
                dbConfig.getHost(),
                dbConfig.getPort(),
                dbConfig.getName(),
                dbConfig.getUsername(),
                dbConfig.getPassword()
        ));
        
        // Check we can connect to the database, and are authenticated.
        DB db = dbHelper.getConnection();
        if (db == null)
        {
            throw new RuntimeException("Error: No database connection available; aborting.");
        }
        if (!db.isAuthenticated())
        {
            throw new RuntimeException("Error: Could not authenticate against database; aborting.");
        }
        
        // Create MONGOJACK connection to the database.
        JacksonDBCollection<Registration, String> documentCollection = JacksonDBCollection.wrap(
            db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);

        // We'll import the records into memory, and only write them to the
        // Mongo database if all files can be parsed successfully.  This will
        // avoid having to recover from "partial success" scenarios.
        int importErrorCount = 0;
        List<Registration>importedRegistrations = new ArrayList<Registration>();
        importErrorCount += importRecordsFromCsvFile(importedRegistrations, irCompanyDataFilePath, out);
        importErrorCount += importRecordsFromCsvFile(importedRegistrations, irPartnersDataFilePath, out);
        importErrorCount += importRecordsFromCsvFile(importedRegistrations, irIndividualDataFilePath, out);
        importErrorCount += importRecordsFromCsvFile(importedRegistrations, irPublicBodyDataFilePath, out);
        
        // Write the registrations to the database ONLY IF all records were
        // successfully read in.
        if (importErrorCount == 0)
        {
            documentCollection.insert(importedRegistrations);
            out.println(String.format("\n==> Successfully imported %d registrations", importedRegistrations.size()));
        }
        else
        {
            out.println("\n==> Not making any changes to database, due to errors during CSV import.");
        }
    }
    
    /**
     * Imports the data from a single CSV file, creating new Registration objects
     * as required, and saves them to the provided list.
     * @param importedRegistrations A list to save newly-imported Registrations to.
     * @param source Full path to a CSV file to import the data from.
     * @param out An object allowing formatted messages to be returned to the
     * REST call originator.
     * @return The number of errors encountered parsing the file / validating
     * the contents of the file.
     * @throws Exception 
     */
    private int importRecordsFromCsvFile(List<Registration> importedRegistrations, String source, PrintWriter out) throws Exception
    {
        CSVReader reader = null;
        String[] rowData;
        int rowIndex = 0, errorCount = 0;
        
        out.println(String.format("\n==> Importing registrations from %s", source));
        
        try
        {
            reader = new CSVReader(new FileReader(source));
            Registration registration = null;
            
            while ((rowData = reader.readNext()) != null)
            {
                rowIndex++;
                
                // Check row isn't empty or a header row.
                if (rowData.length == 1)
                {
                    out.println(String.format("Skipping row %d; row contains only one field.", rowIndex));
                }
                else if ("TIER".equalsIgnoreCase(rowData[0]))
                {
                    out.println(String.format("Skipping row %d; looks like a header row.", rowIndex));
                }
                else
                {
                    // We expect this row to contain valid data; attempt to
                    // process it.
                    try
                    {
                        // Validate row contains expected number of columns, and
                        // that Registration ID column 'appears' valid.
                        if (rowData.length != (CsvColumn.Status.index() + 1))
                        {
                            throw new RuntimeException("unexpected number of columns");
                        }
                        assertMinStringLength(rowData[CsvColumn.RegID.index()], "REGIDENTIFIER", 10);
                        
                        // Either place the data from this row into a new
                        // registration, or append to the registration from the
                        // previous data row.
                        if (registration == null)
                        {
                            registration = createNewRegistrationFromDataRow(rowData);
                        }
                        else if (registration.getRegIdentifier().equals(rowData[CsvColumn.RegID.index()]))
                        {
                            updateRegistrationWithPersonData(registration, rowData);
                        }
                        else
                        {
                            // This row corresponds to a new registration, so
                            // we'll save the data from the previous row now.
                            if (!safelyValidateRegistrationAndSave(importedRegistrations, registration, out))
                            {
                                errorCount++;
                            }
                            registration = createNewRegistrationFromDataRow(rowData);
                        }
                    }
                    // Handle any error processing a single row of data.
                    catch (Exception e)
                    {
                        registration = null;
                        errorCount++;
                        
                        String exceptionMessage = e.getMessage();
                        String message = String.format("Failed to import data from row %d: %s.", rowIndex, exceptionMessage);
                        log.warning(message);
                        out.println(message);
                        if ((exceptionMessage == null) || exceptionMessage.isEmpty())
                        {
                            out.println("Unexpected error: stack trace follows...");
                            e.printStackTrace(out);
                        }
                    }
                } // End of processing this row of data.
            } // End of WHILE loop.
            
            // Save the last registraiton, if necessary
            if (registration != null)
            {
                if (!safelyValidateRegistrationAndSave(importedRegistrations, registration, out))
                {
                    errorCount++;
                }
            }
        }
        
        // Ensure we always (try to) release the CSV file we opened at the top
        // of this function.
        finally
        {
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
            }
            catch (IOException e)
            {
                log.severe(String.format("IOException while closing CSVReader: %s", e.getMessage()));
                out.println(String.format("Error: IOException while closing CSVReader: %s", e.getMessage()));
                throw(e);
            }
        }
        
        // Return the error count.
        return errorCount;
    }
    
    /**
     * Creates a new Registration object and populates it with the provided data
     * (which has been read from a CSV file).
     * @param dataRow Parsed data read from one line of the CSV file.
     * @return A new Registration object populated from the provided data.
     * @throws RuntimeException 
     */
    private Registration createNewRegistrationFromDataRow(String[] dataRow) throws RuntimeException
    {
        Registration registration = new Registration();
        
        // We have already checked that the RegID column contains some data, so
        // don't need any further checks here.
        registration.setRegIdentifier(dataRow[CsvColumn.RegID.index()]);
        
        // Parse and set enumeration-like values.
        setTier(registration, dataRow[CsvColumn.Tier.index()]);
        setCarrierType(registration, dataRow[CsvColumn.CarrierType.index()]);
        setBusinessType(registration, dataRow[CsvColumn.BusinessType.index()]);
        
        // Set other properties of the registration.
        setOrganisationDetails(registration, dataRow);      
        setMiscellaneousRegDetails(registration, dataRow);
        setAddresses(registration, dataRow);
        
        // Parse the Key Person data from this row.
        registration.setKeyPeople(new ArrayList<KeyPerson>());
        updateRegistrationWithPersonData(registration, dataRow);
          
        // Return the Registration as extracted from this row.  It may need to
        // be updated with Key Person data from a subsequent row, but we'll
        // handle this elsewhere.
        return registration;
    }
    
    /**
     * Asserts that the number of Key People for a registration is appropriate
     * for the Business Type, and that a Contact first + last name has been
     * imported.  If all tests pass the Registration is saved.  If any test
     * fails, the error is logged but no exception is thrown.
     * @param importedRegistrations A list to save the new Registration to if it
     * is valid.
     * @param reg The registration object to check, and save if valid.
     * @param out An object allowing formatted messages to be returned to the
     * REST call originator.
     * @return True if the new Registration was valid and saved; otherwise False.
     */
    private boolean safelyValidateRegistrationAndSave(List<Registration> importedRegistrations, Registration reg, PrintWriter out)
    {
        boolean success = false;
        
        try
        {
            // Assert a contact name (first + last) was found.
            String firstName = reg.getFirstName();
            String lastName = reg.getLastName();
            if ((firstName == null) || firstName.isEmpty())
            {
                throw new RuntimeException("no contact first name found");
            }
            if ((lastName == null) || lastName.isEmpty())
            {
                throw new RuntimeException("no contact last name found");
            }

            // Validate the Key People list.  This only applies to Upper Tier registrations.
            if (reg.getTier() == Registration.RegistrationTier.UPPER)
            {
                ArrayList<KeyPerson> keyPeople = (ArrayList<KeyPerson>)reg.getKeyPeople();
                if (keyPeople.isEmpty())
                {
                    throw new RuntimeException("no Key Person identified");
                }
                
                String businessType = reg.getBusinessType();
                if (SOLE_TRADER.equals(businessType) || PUBLIC_BODY.equals(businessType))
                {
                    if (keyPeople.size() > 1)
                    {
                        throw new RuntimeException("multiple Key People found for Sole Trader or Public Body registration");
                    }
                }
                else if (PARTNERSHIP.equals(businessType))
                {
                    if (keyPeople.size() < 2)
                    {
                        throw new RuntimeException("need at least 2 Key People for Partnership registration");
                    }
                }
            }

            // If we've got here the Registration is valid, so we'll save it.
            importedRegistrations.add(reg);
            success = true;
        }
        catch (Exception e)
        {
            String exceptionMessage = e.getMessage();
            String message = String.format("Failed to import registraiton %s: %s.", reg.getRegIdentifier(), exceptionMessage);
            log.warning(message);
            out.println(message);
            if ((exceptionMessage == null) || exceptionMessage.isEmpty())
            {
                out.println("Unexpected error: stack trace follows...");
                e.printStackTrace(out);
            }
        }
        
        return success;
    }
    
    /**
     * Asserts a string is at least the specified minimum length, and throws an
     * exception if not.
     * @param s The string to check for length.
     * @param colName The name of the CSV column the string came from, used when
     * creating an exception description.
     * @param minLength The minimum length for the string.
     * @throws RuntimeException 
     */
    private void assertMinStringLength(String s, String colName, int minLength) throws RuntimeException
    {
        if (s.length() < minLength)
        {
            throw new RuntimeException(String.format("value in %s is too short", colName));
        }
    }
    
    // Sets the Tier from a string.
    private void setTier(Registration reg, String csvValue)
    {
        if ("UPPER".equals(csvValue))
        {   
            reg.setTier(Registration.RegistrationTier.UPPER);
        }
        else if ("LOWER".equals(csvValue))
        {
            reg.setTier(Registration.RegistrationTier.LOWER);
        }
        else
        {
            throw new RuntimeException("unknown value in TIER column");
        }
    }
    
    // Sets the Carrier / Broker / Dealer type.
    private void setCarrierType(Registration reg, String csvValue)
    {
        if ("Carrier".equalsIgnoreCase(csvValue))
        {
            reg.setRegistrationType("carrier_dealer");
        }
        else if ("Broker".equalsIgnoreCase(csvValue))
        {
            reg.setRegistrationType("broker_dealer");
        }
        else if ("Carrier and Broker".equalsIgnoreCase(csvValue))
        {
            reg.setRegistrationType("carrier_broker_dealer");
        }
        else
        {
            throw new RuntimeException("unknown value in REGISTRATIONTYPE column");
        }
    }
    
    // Sets the Business Type (e.g. Company, Sole Trader etc.)
    private void setBusinessType(Registration reg, String csvValue)
    {
        if ("Person".equals(csvValue))
        {
            reg.setBusinessType(SOLE_TRADER);
        }
        else if ("Company".equalsIgnoreCase(csvValue))
        {
            reg.setBusinessType(COMPANY);
        }
        else if ("Public Body".equalsIgnoreCase(csvValue))
        {
            reg.setBusinessType(PUBLIC_BODY);
        }
        else if ("Organisation of Individuals".equalsIgnoreCase(csvValue))
        {
            reg.setBusinessType(PARTNERSHIP);
        }
        else
        {
            throw new RuntimeException("unknown value in BUSINESSTYPE column");
        }
    }
    
    // Sets miscellaneous fields on the registration, including contact email
    // and phone number, registration identifier, AD access code, and all
    // date fields, plus the registration metadata.
    private void setMiscellaneousRegDetails(Registration reg, String[] dataRow)
    {
        // Set miscellaneous contact information.
        assertMinStringLength(dataRow[CsvColumn.PhoneNumber.index()], "PHONENUMBER", 8);
        reg.setPhoneNumber(dataRow[CsvColumn.PhoneNumber.index()]);
        if (!dataRow[CsvColumn.ContactEmail.index()].isEmpty())
        {
            reg.setContactEmail(dataRow[CsvColumn.ContactEmail.index()]);
        }
        
        // Generate a random 6-character Access Code, and set it.
        StringBuilder accessCode = new StringBuilder(ACCESS_CODE_LENGTH);
        for (int n = 0; n < ACCESS_CODE_LENGTH; n++)
        {
            accessCode.append(ACCESS_CODE_CHARS.charAt(accessCodeRnd.nextInt(ACCESS_CODE_CHARS_LENGTH)));
        }
        reg.setAccessCode(accessCode.toString());
        
        // Parse registration dates.  We force each registration to start and
        // expire at mid-day.  This avoids date-boundary and daylight-saving
        // issues.
        Date registrationDate, expiryDate;
        try
        {
            registrationDate = dateParser.parse(dataRow[CsvColumn.RegisteredDate.index()]);
            registrationDate = new DateTime(registrationDate).plusHours(12).toDate();
        }
        catch (ParseException e)
        {
            throw new RuntimeException("cannot parse Registration Date", e);
        }
        
        try
        {
            expiryDate = dateParser.parse(dataRow[CsvColumn.ExpiryDate.index()]);
            expiryDate = new DateTime(expiryDate).plusHours(12).toDate();
        }
        catch (ParseException e)
        {
            throw new RuntimeException("cannot parse Expiry Date", e);
        }
        reg.setExpires_on(expiryDate);
                
        // Set registration metadata.
        if (!"ACTIVE".equals(dataRow[CsvColumn.Status.index()]))
        {
            throw new RuntimeException("STATUS column contains value other than 'ACTIVE'");
        }
        MetaData md = new MetaData();
        md.setAnotherString("Imported-from-IR");
        md.setStatus(MetaData.RegistrationStatus.ACTIVE);
        md.setRoute(MetaData.RouteType.ASSISTED_DIGITAL);
        md.setDistance("n/a");
        md.setDateRegistered(registrationDate);
        md.setDateActivated(registrationDate);
        md.setLastModified(new Date());
        reg.setMetaData(md);
        
        // Set conviction and declaration fields.
        reg.setDeclaration("1");
    }
    
    // Sets the addresses for a registration.
    private void setAddresses(Registration reg, String[] dataRow)
    {
        // Validate addresses have expected data.
        assertMinStringLength(dataRow[CsvColumn.RegAddrBuilding.index()], "REG_HOUSENUMBER", 1);
        assertMinStringLength(dataRow[CsvColumn.RegAddrTown.index()], "REG_TOWNCITY", 2);
        assertMinStringLength(dataRow[CsvColumn.RegAddrPostcode.index()], "REG_POSTCODE", 5);
        
        assertMinStringLength(dataRow[CsvColumn.PostAddrBuilding.index()], "POST_HOUSENUMBER", 1);
        assertMinStringLength(dataRow[CsvColumn.PostAddrTown.index()], "POST_TOWNCITY", 2);
        assertMinStringLength(dataRow[CsvColumn.PostAddrPostcode.index()], "POST_POSTCODE", 5);
        
        // Create and populate Registered address.
        Address regAddr = new Address();
        regAddr.setAddressType(Address.addressType.REGISTERED);
        regAddr.setAddressMode("manual-uk");
        regAddr.setHouseNumber(dataRow[CsvColumn.RegAddrBuilding.index()]);
        regAddr.setTownCity(dataRow[CsvColumn.RegAddrTown.index()]);
        regAddr.setPostcode(dataRow[CsvColumn.RegAddrPostcode.index()]);
        setAddressLines(regAddr, dataRow, CsvColumn.RegAddrLine1.index());
        regAddr.setEasting(dataRow[CsvColumn.RegAddrEasting.index()]);
        regAddr.setNorthing(dataRow[CsvColumn.RegAddrNorthing.index()]);
        
        // Create and populate Postal address.
        Address postalAddr = new Address();
        postalAddr.setAddressType(Address.addressType.POSTAL);
        postalAddr.setHouseNumber(dataRow[CsvColumn.PostAddrBuilding.index()]);
        postalAddr.setTownCity(dataRow[CsvColumn.PostAddrTown.index()]);
        postalAddr.setPostcode(dataRow[CsvColumn.PostAddrPostcode.index()]);
        setAddressLines(postalAddr, dataRow, CsvColumn.PostAddrLine1.index());
        
        reg.setAddresses(Arrays.asList(regAddr, postalAddr));
    }
    
    // Sets the AddressLine properties for an address, ignoring columns which
    // are blank.
    private void setAddressLines(Address address, String[] dataRow, int firstLineIndex)
    {
        // There are currently at most 4 generic lines in the address.
        int maxLines = 4;
        String[] nonEmptyLines = new String[maxLines];
        
        // Make a copy of the address lines in a new array, but only keeping
        // the non-empty lines.
        int targetLine = 0;
        for (int n = 0; n < maxLines; n++)
        {
            if (!dataRow[firstLineIndex + n].isEmpty())
            {
                nonEmptyLines[targetLine++] = dataRow[firstLineIndex + n];
            }
        }
        
        // Use the result to populate the address object.  Fail if we didn't 
        // find at least one non-empty line.
        if ((nonEmptyLines[0] == null) || nonEmptyLines[0].isEmpty())
        {
            throw new RuntimeException("All ADDRESSLINE columns are empty.");
        }
        else
        {
            address.setAddressLine1(nonEmptyLines[0]);
        }
        
        if ((nonEmptyLines[1] != null) && !nonEmptyLines[1].isEmpty())
        {
            address.setAddressLine2(nonEmptyLines[1]);
        }
        if ((nonEmptyLines[2] != null) && !nonEmptyLines[2].isEmpty())
        {
            address.setAddressLine3(nonEmptyLines[2]);
        }
        if ((nonEmptyLines[3] != null) && !nonEmptyLines[3].isEmpty())
        {
            address.setAddressLine4(nonEmptyLines[3]);
        }
    }
    
    // Sets the organisation name, and (if the organisation is a limited company)
    // also sets the company number.
    private void setOrganisationDetails(Registration reg, String[] dataRow)
    {
        // If the registration has ALREADY been set to have Organisation Type of
        // limited company, then sets company number.
        if ("limitedCompany".equals(reg.getBusinessType()))
        {
            assertMinStringLength(dataRow[CsvColumn.CompanyNo.index()], "COMPANYNO", 6);
            reg.setCompanyNo(dataRow[CsvColumn.CompanyNo.index()]);
        }
        
        // Take the orgnisation name from the Business Name column if possible,
        // or the Post Name column (sole trader and public body only) otherwise.
        if (dataRow[CsvColumn.BusinessName.index()].length() > 2)
        {
            reg.setCompanyName(dataRow[CsvColumn.BusinessName.index()]);
        }
        else if ((dataRow[CsvColumn.PostAddrName.index()].length() > 2)
                && ("soleTrader".equals(reg.getBusinessType())) || "publicBody".equals(reg.getBusinessType()))
        {
            reg.setCompanyName(dataRow[CsvColumn.PostAddrName.index()]);
        }
        else
        {
            throw new RuntimeException("couldn't find suitable value for Organisation Name");
        }
    }
    
    // Updates a Registration with data from the person-related columns in the
    // CSV file.  These columns may contain data about a Key Person, and/or
    // the contact name for this Registration.
    private void updateRegistrationWithPersonData(Registration reg, String[] dataRow)
    {
        // Extract some key values for easier manipulation.
        String businessType = reg.getBusinessType();
        String personPosition = dataRow[CsvColumn.Position.index()];
        
        // Decide if the person on this row of the CSV file should be used as
        // the main contact for this Registration, and store their name if so.
        boolean useAsContact = "Contact".equalsIgnoreCase(personPosition);
        if (PUBLIC_BODY.equals(businessType))
        {
            useAsContact &= dataRow[CsvColumn.ApplicantName.index()].toLowerCase()
                    .contains(dataRow[CsvColumn.Lastname.index()].toLowerCase());
        }
        
        if (useAsContact)
        {
            reg.setFirstName(dataRow[CsvColumn.Firstname.index()]);
            reg.setLastName(dataRow[CsvColumn.Lastname.index()]);
        }
        
        // Key People are only applicable on Upper Tier registrations.
        if (reg.getTier() == Registration.RegistrationTier.UPPER)
        {
            // Should the person on this line of the CSV file be added to the list
            // of Key People for this Registration?
            String serrSimPositionName = "";
            if (COMPANY.equals(businessType) && "Company Officer".equalsIgnoreCase(personPosition))
            {
                serrSimPositionName = "Director";
            }
            else if (PARTNERSHIP.equals(businessType) && "Group Member".equalsIgnoreCase(personPosition))
            {
                serrSimPositionName = "Partner";
            }
            else if (PUBLIC_BODY.equals(businessType) && "Company Officer".equalsIgnoreCase(personPosition))
            {
                serrSimPositionName = "CEO";
            }
            else if (SOLE_TRADER.equals(businessType))
            {
                serrSimPositionName = "Business owner";
            }

            if (!serrSimPositionName.isEmpty())
            {
                Date personDateOfBirth;
                try
                {
                    personDateOfBirth = dateParser.parse(dataRow[CsvColumn.DateOfBirth.index()]);
                }
                catch (ParseException e)
                {
                    throw new RuntimeException("cannot parse person's Date Of Birth", e);
                }

                KeyPerson keyPerson = new KeyPerson();
                keyPerson.setFirstName(dataRow[CsvColumn.Firstname.index()]);
                keyPerson.setLastName(dataRow[CsvColumn.Lastname.index()]);
                keyPerson.setDateOfBirth(personDateOfBirth);
                keyPerson.setPosition(serrSimPositionName);

                ArrayList<KeyPerson> keyPeople = (ArrayList<KeyPerson>)reg.getKeyPeople();
                keyPeople.add(keyPerson);
                reg.setKeyPeople(keyPeople);
            }
        }
    }
}
