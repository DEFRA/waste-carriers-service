package uk.gov.ea.wastecarrier.services.cli;

import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.config.Bootstrap;

import java.io.*;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.mongodb.DB;
import net.vz.mongodb.jackson.JacksonDBCollection;

import au.com.bytecode.opencsv.CSVReader;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.joda.time.DateTime;

import uk.gov.ea.wastecarrier.services.WasteCarrierConfiguration;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
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
 * java -jar <jarfile> irimport -s <csvfile> <configuration.yml>
 *
 * There are no Query String parameters.
 */
public class IRImporter extends ConfiguredCommand<WasteCarrierConfiguration>
{
    // Private static members.
    private static final SecureRandom accessCodeRnd = new SecureRandom();
    
    // Private constants and enumerations.
    private static final int ACCESS_CODE_LENGTH = 6;
    private static final String ACCESS_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
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
        RegAddrBuilding  (8),
        RegAddrLine1     (9),
        RegAddrLine2     (10),
        RegAddrLine3     (11),
        RegAddrLine4     (12),
        RegAddrTown      (13),
        RegAddrPostcode  (14),
        RegAddrEasting   (15),
        RegAddrNorthing  (16),
        PostAddrBuilding (17),
        PostAddrLine1    (18),
        PostAddrLine2    (19),
        PostAddrLine3    (20),
        PostAddrLine4    (21),
        PostAddrTown     (22),
        PostAddrPostcode (23),
        PostAddrName     (24),
        Firstname        (25),
        Lastname         (26),
        DateOfBirth      (27),
        Position         (28),
        RegID            (30),
        ExpiryDate       (31),
        RegisteredDate   (32),
        Status           (33);
                
        private final int value;

        private CsvColumn(int value) {
          this.value = value;
        }

        public int index() {
          return value;
        }
    }
        
    // Private instance members set during command execution.
    private SimpleDateFormat dateParser;

    /**
     * Constructor.  Sets command name and description only.
     */
    public IRImporter()
    {
        super("irimport", "Imports IR registrations from a CSV file");
    }
    
    /**
     * Describes the command-line arguments required to run this command.
     * @param subparser (See DropWizard documentation).
     */
    @Override
    public void configure(Subparser subparser)
    {
        super.configure(subparser);
        
        subparser.addArgument("-s", "--source")
                .dest("source")
                .type(String.class)
                .required(true)
                .help("full path to the CSV file to import data from");
        
        subparser.addArgument("--dateformat")
                .dest("dateFormat")
                .type(String.class)
                .setDefault("dd/MM/yyyy")
                .help("the format to use when parsing date values");
        
        subparser.addArgument("--dryrun")
                .dest("dryrun")
                .action(Arguments.storeTrue())
                .help("perform a dry run only; don't modify the database");
    }
    
    /**
     * Runs the command
     * @param bootstrap The DropWizard bootstrap (not used).
     * @param namespace The DropWizard parsed command line namespace.
     * @param configuration Service configuration read from a YAML file.
     * @throws Exception 
     */
    @Override
    public void run(Bootstrap<WasteCarrierConfiguration> bootstrap, Namespace namespace, WasteCarrierConfiguration configuration) throws Exception
    {
        // Create a date parser.
        dateParser = new SimpleDateFormat(namespace.getString("dateFormat"));
        
        // Get database configuration, read in from YAML file.
        DatabaseConfiguration dbConfig = configuration.getDatabase();
        
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
        List<Registration>importedRegistrations = new ArrayList<Registration>();
        int errorCount = importRecordsFromCsvFile(importedRegistrations, namespace.getString("source"));
        
        // Write the registrations to the database ONLY if all records were
        // successfully read in.
        if ((errorCount == 0) && !namespace.getBoolean("dryrun"))
        {
            System.out.println("\n==> Writing imported registrations to database...");
            documentCollection.insert(importedRegistrations);
            System.out.println(String.format("\n==> Successfully imported %d registrations", importedRegistrations.size()));
        }
        else if (errorCount != 0)
        {
            System.out.println("\n==> Not making any changes to database, due to errors during CSV import.");
        }
        else
        {
            System.out.println("\n==> No parsing errors encountered.  No changes to database; dry-run only.");
        }
    }
    
    /**
     * Imports the data from a single CSV file, creating new Registration objects
     * as required, and saves them to the provided list.
     * @param importedRegistrations A list to save newly-imported Registrations to.
     * @param source Full path to a CSV file to import the data from.
     * @return The number of errors encountered parsing the file / validating
     * the contents of the file.
     * @throws Exception 
     */
    private int importRecordsFromCsvFile(List<Registration> importedRegistrations, String source) throws Exception
    {
        CSVReader reader = null;
        String[] rowData;
        int rowIndex = 0, errorCount = 0;
        
        System.out.println(String.format("\n==> Importing registrations from %s", source));
        
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
                    System.out.println(String.format("Skipping row %d; row contains only one field.", rowIndex));
                }
                else if ("TIER".equalsIgnoreCase(rowData[0]))
                {
                    System.out.println(String.format("Skipping row %d; looks like a header row.", rowIndex));
                }
                else
                {
                    // We expect this row to contain valid data; attempt to
                    // process it.
                    try
                    {
                        // Check row contains expected number of columns, and
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
                            if (!safelyValidateRegistrationAndSave(importedRegistrations, registration))
                            {
                                errorCount++;
                            }
                            registration = createNewRegistrationFromDataRow(rowData);
                        }
                    }
                    // Handle any error that occurred processing this row of data.
                    catch (Exception e)
                    {
                        registration = null;
                        errorCount++;
                        
                        String exceptionMessage = e.getMessage();
                        String message = String.format("Failed to import data from row %d: %s.", rowIndex, exceptionMessage);
                        System.out.println(message);
                        if ((exceptionMessage == null) || exceptionMessage.isEmpty())
                        {
                            System.out.println("Unexpected error: stack trace follows...");
                            e.printStackTrace(System.out);
                        }
                    }
                } // End of processing this row of data.
            } // End of WHILE loop iterating over all rows in CSV file.
            
            // Save the last registraiton in the file, if necessary.
            if (registration != null)
            {
                if (!safelyValidateRegistrationAndSave(importedRegistrations, registration))
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
                System.out.println(String.format("Error: IOException while closing CSVReader: %s", e.getMessage()));
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
        if (registration.getTier() == Registration.RegistrationTier.UPPER)
        {
            registration.setKeyPeople(new ArrayList<KeyPerson>());
        }
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
     * @return True if the new Registration was valid and saved; otherwise False.
     */
    private boolean safelyValidateRegistrationAndSave(List<Registration> importedRegistrations, Registration reg)
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
            System.out.println(message);
            if ((exceptionMessage == null) || exceptionMessage.isEmpty())
            {
                System.out.println("Unexpected error: stack trace follows...");
                e.printStackTrace(System.out);
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
        if ((s == null) || (s.length() < minLength))
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
    
    // Sets the Business Type (e.g. Company, Sole Trader etc).
    private void setBusinessType(Registration reg, String csvValue)
    {
        if ("Person".equalsIgnoreCase(csvValue))
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
    // and phone number, AD access code, all date fields, plus the
    // registration metadata.
    private void setMiscellaneousRegDetails(Registration reg, String[] dataRow)
    {
        // Set phone number.
        String phoneNumber = dataRow[CsvColumn.PhoneNumber.index()];
        assertMinStringLength(phoneNumber, "PHONENUMBER", 8);
        reg.setPhoneNumber(phoneNumber);
        
        // Set email address if provided.
        String emailAddress = dataRow[CsvColumn.ContactEmail.index()];
        if ((emailAddress != null) && !emailAddress.isEmpty())
        {
            reg.setContactEmail(emailAddress);
        }
        
        // Generate an Assisted Digital user Access Code.
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
        
        // Set declaration field.
        reg.setDeclaration("1");
        
        // TODO: Testing to determine if conviction-check fields are required.
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
        String businessType = reg.getBusinessType();
        
        // If the registration has ALREADY been set to have Organisation Type of
        // limited company, then set company number.
        if (COMPANY.equals(businessType))
        {
            assertMinStringLength(dataRow[CsvColumn.CompanyNo.index()], "COMPANYNO", 6);
            reg.setCompanyNo(dataRow[CsvColumn.CompanyNo.index()]);
        }
        
        // Take the orgnisation name from the Business Name column if possible,
        // or the Post Name column (sole trader and public body only) otherwise.
        String businessName = dataRow[CsvColumn.BusinessName.index()];
        String postAddrName = dataRow[CsvColumn.PostAddrName.index()];
        if (businessName.length() > 2)
        {
            reg.setCompanyName(businessName);
        }
        else if ((postAddrName.length() > 2) && (SOLE_TRADER.equals(businessType) || PUBLIC_BODY.equals(businessType)))
        {
            reg.setCompanyName(postAddrName);
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
        if (useAsContact && PUBLIC_BODY.equals(businessType))
        {
            // Public Bodies seem to have multiple Contact people.  We try to
            // select the best one by ideally choosing the Person whose surname
            // appears in the "Applicant" column.
            boolean isBestContact = dataRow[CsvColumn.ApplicantName.index()].toLowerCase()
                    .contains(dataRow[CsvColumn.Lastname.index()].toLowerCase());
            
            // But in case we don't find anyone matching that criteria, we'll
            // default to the first Contact we find.
            String existingContactLastname = reg.getLastName();
            boolean noExistingConact = (existingContactLastname == null) || existingContactLastname.isEmpty();
            
            // Use this Person if either condition is met.
            useAsContact = isBestContact || noExistingConact;
        }
        
        if (useAsContact)
        {
            assertMinStringLength(dataRow[CsvColumn.Firstname.index()], "FIRSTNAME", 1);
            assertMinStringLength(dataRow[CsvColumn.Lastname.index()], "SURNAME", 2);
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
                // Validate the name and DoB of this Key Person.
                assertMinStringLength(dataRow[CsvColumn.Firstname.index()], "FIRSTNAME", 1);
                assertMinStringLength(dataRow[CsvColumn.Lastname.index()], "SURNAME", 2);
            
                Date personDateOfBirth;
                try
                {
                    personDateOfBirth = dateParser.parse(dataRow[CsvColumn.DateOfBirth.index()]);
                }
                catch (ParseException e)
                {
                    throw new RuntimeException("cannot parse person's Date Of Birth", e);
                }

                // Add this person to the list of Key People.
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
