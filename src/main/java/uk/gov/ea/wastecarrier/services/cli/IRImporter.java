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
import java.util.TimeZone;
import java.util.UUID;

import com.mongodb.DB;
import org.mongojack.JacksonDBCollection;

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
import uk.gov.ea.wastecarrier.services.core.FinanceDetails;
import uk.gov.ea.wastecarrier.services.core.KeyPerson;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Order;
import uk.gov.ea.wastecarrier.services.core.OrderItem;

/**
 * Imports registration records from CSV files.  It is intended to be used only
 * once, to migrate Waste Carrier Registration data from IR into the new digital
 * service.
 *
 * java -jar <jarfile> irimport <configuration.yml> -s <csvfile>
 *
 * There are no Query String parameters.
 */
public class IRImporter extends ConfiguredCommand<WasteCarrierConfiguration>
{
    // Private static members.
    private static final SecureRandom RND_SOURCE = new SecureRandom();
    
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
    private int nActions = 0, nRecommendations = 0;

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
        // Output useful logging.
        System.out.println("IR-Import command starting");
        System.out.println(String.format(" - will attempt to import from %s", namespace.getString("source")));
        System.out.println(String.format(" - will attempt to parse dates using the format string '%s'", namespace.getString("dateFormat")));
        System.out.println(namespace.getBoolean("dryrun") ?
                " - using Dry Run mode; no changes will be made to the database" :
                " - not a Dry Run; will update database if no errors occur");
        System.out.println();
        
        // Create a date parser.
        dateParser = new SimpleDateFormat(namespace.getString("dateFormat"));
        dateParser.setTimeZone(TimeZone.getTimeZone("UTC"));
        
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
        
        // Create MONGOJACK connection to the database.
        JacksonDBCollection<Registration, String> documentCollection = JacksonDBCollection.wrap(
            db.getCollection(Registration.COLLECTION_NAME), Registration.class, String.class);

        // We'll import the records into memory, and only write them to the
        // Mongo database if all files can be parsed successfully.  This will
        // avoid having to recover from "partial success" scenarios.
        List<Registration>importedRegistrations = new ArrayList<Registration>();
        int errorCount = importRecordsFromCsvFile(importedRegistrations, namespace.getString("source"));
        
        // Print a summary of the number of Errors, Actions and Recommendations.
        System.out.println();
        System.out.println(String.format("Total number of Errors: %d", errorCount));
        System.out.println(String.format("Total number of Actions: %d", nActions));
        System.out.println(String.format("Total number of Recommendations: %d", nRecommendations));
        System.out.println(String.format("Total number of Registrations ready for import: %d", importedRegistrations.size()));
        
        // Write the registrations to the database ONLY if all records were
        // successfully read in.
        System.out.println();
        if ((errorCount == 0) && !namespace.getBoolean("dryrun"))
        {
            System.out.println("==> Writing imported registrations to database...");
            documentCollection.insert(importedRegistrations);
            System.out.println(String.format("==> Successfully imported %d registrations", importedRegistrations.size()));
        }
        else if (errorCount != 0)
        {
            System.out.println("==> Not making any changes to database, due to errors during CSV import.");
        }
        else
        {
            System.out.println("==> No parsing errors encountered.  No changes to database; dry-run only.");
        }
        
        System.out.println("\nIR-Import command exiting cleanly");
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
        String[] rowData, previousRowData;
        int rowIndex = 0, errorCount = 0;
        
        try
        {
            reader = new CSVReader(new FileReader(source));
            Registration registration = null;
            previousRowData = null;
            
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
                else if (rowData.length != (CsvColumn.Status.index() + 1))
                {
                    throw new RuntimeException(String.format("Aborting import: unexpected number of columns in row %d", rowIndex));
                }
                else if (Arrays.deepEquals(rowData, previousRowData))
                {
                    System.out.println(String.format("Skipping row %d; it is identical to the previous row", rowIndex));
                }
                else if (!"ACTIVE".equals(rowData[CsvColumn.Status.index()]))
                {
                    System.out.println(String.format("Skipping row %d; status is %s and not ACTIVE (%s)", rowIndex,
                            rowData[CsvColumn.Status.index()], rowData[CsvColumn.RegID.index()]));
                }
                else
                {
                    // We expect this row to contain valid data; attempt to
                    // process it.
                    try
                    {
                        // Update our record of the "previous" row.
                        previousRowData = rowData;
                        
                        // Check that Registration ID column 'appears' valid.
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
                            updateRegistrationWithContactPersonData(registration, rowData);
                            updateRegistrationWithKeyPersonData(registration, rowData);
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
                        if (stringIsNullOrEmpty(exceptionMessage))
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
        updateRegistrationWithContactPersonData(registration, dataRow);
        updateRegistrationWithKeyPersonData(registration, dataRow);
        
        // Add Finance Details if an Upper Tier registration.
        if (registration.getTier() == Registration.RegistrationTier.UPPER)
        {
            addDummyFinanceDetails(registration);
        }
        
        // Return the Registration as extracted from this row.  It may need to
        // be updated with Key Person data from a subsequent row, but we'll
        // handle this elsewhere.
        return registration;
    }
    
    /**
     * Checks that the number of Key People for a registration is appropriate
     * for the Business Type, and that a Contact first + last name has been
     * imported.  Missing or invalid data is filled-in with dummy data, and
     * warnings to fix this manually later are issued.  If any error occurs it
     * is logged but no Exception is thrown.
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
            // Provide a dummy contact name if no real name was found.
            if (stringIsNullOrEmpty(reg.getFirstName()) || stringIsNullOrEmpty(reg.getLastName()))
            {
                nActions++;
                reg.setFirstName("The");
                reg.setLastName("Waste Carrier Registrant");
                System.out.println(String.format("Action: correct the Contact Name for %s", reg.getRegIdentifier()));
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
                    if (keyPeople.size() == 1)
                    {
                        System.out.println(String.format("Warning: %s is being added as a Partnership with only one partner", reg.getRegIdentifier()));
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
            if (stringIsNullOrEmpty(exceptionMessage))
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
    
    /**
     * Checks if a string is null, empty or contains only whitespace.
     * @param s The string to check.
     * @return True if the string is null, empty or contains only whitespace;
     * otherwise False.
     */
    private boolean stringIsNullOrEmpty(String s)
    {
        return ((s == null) || s.trim().isEmpty());
    }
    
    /**
     * Converts a string to title case.  Any letter that does not immediately
     * follow another letter is treated as the start of a new word.
     * @param s The string to convert.
     * @return The input string converted into title case, or an empty string
     * if the input is null or contains only whitespace.
     */
    private String toTitleCase(String s)
    {
        if (stringIsNullOrEmpty(s))
        {
            return "";
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            boolean makeNextCharUppercase = true;
            
            for (char c : s.trim().toCharArray())
            {
                sb.append(makeNextCharUppercase ? Character.toUpperCase(c) : Character.toLowerCase(c));
                makeNextCharUppercase = !Character.isLetter(c);
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Safely converts a string to upper case.
     * @param s The string to convert.
     * @return The string converted to upper case, or an empty string if the
     * input is null or contains only whitespace
     */
    private String safeToUpperCase(String s)
    {
        return (stringIsNullOrEmpty(s) ? "" : s.trim().toUpperCase());
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
        if (stringIsNullOrEmpty(phoneNumber) || (phoneNumber.length() < 7))
        {
            System.out.println(String.format("Recommended: add or correct the contact phone number for %s", reg.getRegIdentifier()));
        }
        if (!stringIsNullOrEmpty(phoneNumber))
        {
            reg.setPhoneNumber(phoneNumber);
        }

        
        // Set email address if provided.
        String emailAddress = dataRow[CsvColumn.ContactEmail.index()];
        if (!stringIsNullOrEmpty(emailAddress))
        {
            reg.setContactEmail(emailAddress);
        }
        
        // Generate an Assisted Digital user Access Code.
        StringBuilder accessCode = new StringBuilder(ACCESS_CODE_LENGTH);
        for (int n = 0; n < ACCESS_CODE_LENGTH; n++)
        {
            accessCode.append(ACCESS_CODE_CHARS.charAt(RND_SOURCE.nextInt(ACCESS_CODE_CHARS_LENGTH)));
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
        // Prevent repetitive address warnings.
        boolean regAddrWarningIssued = false, postalAddrWarningIssued = false;
        
        // Check that both addresses have a postcode; issue an Action if not.
        // Ideally we want a premises number / name and town to always be
        // present, but a large proportion of IR data is missing one or both of
        // these fields so we silently ignore this.
        if (stringIsNullOrEmpty(dataRow[CsvColumn.RegAddrPostcode.index()]))
        {
            nActions++;
            regAddrWarningIssued = true;
            System.out.println(String.format("Action: fix the missing postcode in the Registered Address for %s", reg.getRegIdentifier()));
        }
        if (stringIsNullOrEmpty(dataRow[CsvColumn.PostAddrPostcode.index()]))
        {
            nActions++;
            postalAddrWarningIssued = true;
            System.out.println(String.format("Action: fix the missing postcode in the Postal Address for %s", reg.getRegIdentifier()));
        }
        
        // Create and populate Registered address.
        Address regAddr = new Address();
        regAddr.setAddressType(Address.addressType.REGISTERED);
        regAddr.setAddressMode("manual-uk");
        regAddr.setHouseNumber(toTitleCase(dataRow[CsvColumn.RegAddrBuilding.index()]));
        regAddr.setTownCity(toTitleCase(dataRow[CsvColumn.RegAddrTown.index()]));
        regAddr.setPostcode(safeToUpperCase(dataRow[CsvColumn.RegAddrPostcode.index()]));
        setAddressLines(regAddr, dataRow, CsvColumn.RegAddrLine1.index());
        if (stringIsNullOrEmpty(dataRow[CsvColumn.RegAddrEasting.index()]) || stringIsNullOrEmpty(dataRow[CsvColumn.RegAddrNorthing.index()]))
        {
            if (!regAddrWarningIssued)
            {
                nRecommendations++;
                regAddrWarningIssued = true;
                System.out.println(String.format("Recommendation: improve the Registered Address for %s", reg.getRegIdentifier()));
            }
        }
        else
        {
            regAddr.setEasting(dataRow[CsvColumn.RegAddrEasting.index()]);
            regAddr.setNorthing(dataRow[CsvColumn.RegAddrNorthing.index()]);
        }
        
        // If there was no premises and no address lines, issue a warning (Registered address).
        if (stringIsNullOrEmpty(regAddr.getHouseNumber()) && stringIsNullOrEmpty(regAddr.getAddressLine1()))
        {
            if (stringIsNullOrEmpty(regAddr.getPostcode()) && stringIsNullOrEmpty(regAddr.getTownCity()))
            {
                throw new RuntimeException(String.format("Registered Address is completely empty for %s", reg.getRegIdentifier()));
            }
            else if (!regAddrWarningIssued)
            {
                nRecommendations++;
                regAddrWarningIssued = true;  // Intentional unused assignment, in case of future edits.
                System.out.println(String.format("Recommendation: improve the Registered Address for %s", reg.getRegIdentifier()));
            }
        }
        
        // Create and populate Postal address.
        Address postalAddr = new Address();
        postalAddr.setAddressType(Address.addressType.POSTAL);
        postalAddr.setHouseNumber(toTitleCase(dataRow[CsvColumn.PostAddrBuilding.index()]));
        postalAddr.setTownCity(toTitleCase(dataRow[CsvColumn.PostAddrTown.index()]));
        postalAddr.setPostcode(safeToUpperCase(dataRow[CsvColumn.PostAddrPostcode.index()]));
        setAddressLines(postalAddr, dataRow, CsvColumn.PostAddrLine1.index());
        
        // If there was no premises and no address lines, issue a warning (Contact address).
        if (stringIsNullOrEmpty(postalAddr.getHouseNumber()) && stringIsNullOrEmpty(postalAddr.getAddressLine1()))
        {
            if (stringIsNullOrEmpty(postalAddr.getPostcode()) && stringIsNullOrEmpty(postalAddr.getTownCity()))
            {
                throw new RuntimeException(String.format("Postal Address is completely empty for %s", reg.getRegIdentifier()));
            }
            else if (!postalAddrWarningIssued)
            {
                nRecommendations++;
                postalAddrWarningIssued = true;  // Intentional unused assignment, in case of future edits.
                System.out.println(String.format("Recommendation: improve the Postal Address for %s", reg.getRegIdentifier()));
            }
        }
        
        // Save the address data to the registration.
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
        address.setAddressLine1(toTitleCase(nonEmptyLines[0]));
        if (!stringIsNullOrEmpty(nonEmptyLines[1]))
        {
            address.setAddressLine2(toTitleCase(nonEmptyLines[1]));
        }
        if (!stringIsNullOrEmpty(nonEmptyLines[2]))
        {
            address.setAddressLine3(toTitleCase(nonEmptyLines[2]));
        }
        if (!stringIsNullOrEmpty(nonEmptyLines[3]))
        {
            address.setAddressLine4(toTitleCase(nonEmptyLines[3]));
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
            if (stringIsNullOrEmpty(dataRow[CsvColumn.CompanyNo.index()]))
            {
                nRecommendations++;
                System.out.println(String.format("Recommendation: add the Company Number for %s", reg.getRegIdentifier()));
            }
            else
            {
                reg.setCompanyNo(dataRow[CsvColumn.CompanyNo.index()]);
                int regNumLength = dataRow[CsvColumn.CompanyNo.index()].length();
                if (!((regNumLength == 6) || (regNumLength == 8)))
                {
                    nRecommendations++;
                    System.out.println(String.format("Recommendation: check the Company Number for %s", reg.getRegIdentifier()));
                }
            }
        }
        
        // Take the orgnisation name from the Business Name column if possible,
        // or the Post Name column (sole trader and public body only) otherwise.
        String businessName = dataRow[CsvColumn.BusinessName.index()];
        String applicantName = dataRow[CsvColumn.ApplicantName.index()];
        if (!stringIsNullOrEmpty(businessName))
        {
            reg.setCompanyName(businessName);
        }
        else if ((!stringIsNullOrEmpty(applicantName)) && (!COMPANY.equals(businessType)))
        {
            reg.setCompanyName(applicantName);
        }
        else
        {
            throw new RuntimeException("couldn't find suitable value for Organisation Name");
        }
    }
    
    // Updates a Registration with Contact Name data, if this row of the CSV
    // file contains this type of data.
    private void updateRegistrationWithContactPersonData(Registration reg, String[] dataRow)
    {
        // Get the Postal Address for this Registration.
        Address postalAddr = reg.getFirstAddressByType(Address.addressType.POSTAL);
        if (postalAddr == null)
        {
            throw new RuntimeException("Unexpected error: registration is missing a postal address");
        }
        
        // Decide if the person on this row of the CSV file should be used as
        // the main contact for this Registration, and store their name if so.
        // In cases where there are multiple Contacts, we simply use the first.
        if (stringIsNullOrEmpty(reg.getLastName()) && "Contact".equalsIgnoreCase(dataRow[CsvColumn.Position.index()]))
        {
            assertMinStringLength(dataRow[CsvColumn.Firstname.index()], "FIRSTNAME", 1);
            assertMinStringLength(dataRow[CsvColumn.Lastname.index()], "SURNAME", 2);
            reg.setFirstName(dataRow[CsvColumn.Firstname.index()]);
            reg.setLastName(dataRow[CsvColumn.Lastname.index()]);
            postalAddr.setFirstName(dataRow[CsvColumn.Firstname.index()]);
            postalAddr.setLastName(dataRow[CsvColumn.Lastname.index()]);
        }
        
        // For Person type records only, the IR export does not contain a
        // separate row for the Contact.  So we have to try to parse the Post
        // Name column into a First and Last name.
        if (SOLE_TRADER.equals(reg.getBusinessType()) && !stringIsNullOrEmpty(dataRow[CsvColumn.PostAddrName.index()]))
        {
            String[] nameParts = dataRow[CsvColumn.PostAddrName.index()].split("\\s+");
            if ((nameParts != null) && (nameParts.length > 0))
            {
                StringBuilder firstNames = new StringBuilder();
                for (int n = 0, nMax = nameParts.length - 1; n < nMax; n++)
                {
                    if (n > 0)
                    {
                        firstNames.append(" ");
                    }
                    firstNames.append(nameParts[n]);
                }
                
                String firstName = firstNames.toString();
                String lastName = nameParts[nameParts.length - 1];
                
                reg.setFirstName(firstName);
                reg.setLastName(lastName);
                postalAddr.setFirstName(firstName);
                postalAddr.setLastName(lastName);
            }
        }
    }
    
    // Updates a Registration with Key Person data, if this row of the CSV file
    // contains this type of data.
    private void updateRegistrationWithKeyPersonData(Registration reg, String[] dataRow)
    {
        // Extract some values for easier manipulation.
        String businessType = reg.getBusinessType();
        String personPosition = dataRow[CsvColumn.Position.index()];
        
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
                    nActions++;
                    System.out.println(String.format("Action: correct the Date Of Birth for %s %s in %s",
                            dataRow[CsvColumn.Firstname.index()], dataRow[CsvColumn.Lastname.index()], reg.getRegIdentifier()));
                    
                    // Rails app will error without a DoB, so lets create a fake one for now.
                    personDateOfBirth = new Date(0, 0, 1);
                }
                
                // Have we already added this person from a previous row in the
                // file?  This could be from 2 or more rows ago.
                ArrayList<KeyPerson> keyPeople = (ArrayList<KeyPerson>)reg.getKeyPeople();
                boolean isNewPerson = true;
                for (KeyPerson otherPerson : keyPeople)
                {
                    if (dataRow[CsvColumn.Firstname.index()].equals(otherPerson.getFirstName())
                            && dataRow[CsvColumn.Lastname.index()].equals(otherPerson.getLastName())
                            && personDateOfBirth.equals(otherPerson.getDateOfBirth()))
                    {
                        isNewPerson = false;
                        System.out.println(String.format("Skipping repeated Key Person for registraiton %s", reg.getRegIdentifier()));
                    }
                }

                // Add this person to the list of Key People.
                if (isNewPerson)
                {
                    KeyPerson keyPerson = new KeyPerson();
                    keyPerson.setPersonType(KeyPerson.PersonType.KEY);
                    keyPerson.setFirstName(dataRow[CsvColumn.Firstname.index()]);
                    keyPerson.setLastName(dataRow[CsvColumn.Lastname.index()]);
                    keyPerson.setDateOfBirth(personDateOfBirth);
                    keyPerson.setPosition(serrSimPositionName);
                    keyPeople.add(keyPerson);
                }
            }
        }
    }
    
    /**
     * Creates the bare minimum Finance Details required for an Upper Tier
     * registration to function correctly in the service.
     * @param reg The Registration to add Finance Details to.
     */
    private void addDummyFinanceDetails(Registration reg)
    {
        OrderItem dummyOrderItem = new OrderItem();
        dummyOrderItem.setAmount(0);
        dummyOrderItem.setCurrency("GBP");
        dummyOrderItem.setDescription("Import from IR");
        dummyOrderItem.setType(OrderItem.OrderItemType.IR_IMPORT);
        dummyOrderItem.setReference("Reg: " + reg.getRegIdentifier());
        
        Order dummyOrder = new Order();
        dummyOrder.setOrderId(UUID.randomUUID().toString());
        dummyOrder.setOrderCode(Integer.toString(RND_SOURCE.nextInt(999999999)));  // Add an upper bound to avoid negative numbers.
        dummyOrder.setPaymentMethod(Order.PaymentMethod.OFFLINE);
        dummyOrder.setMerchantId("n/a");
        dummyOrder.setWorldPayStatus("n/a");
        dummyOrder.setTotalAmount(0);
        dummyOrder.setCurrency("GBP");
        dummyOrder.setDateCreated(reg.getMetaData().getDateRegistered());
        dummyOrder.setDateLastUpdated(new Date());
        dummyOrder.setDescription("Import from IR");
        dummyOrder.setOrderItems(Arrays.asList(dummyOrderItem));
        
        FinanceDetails dummyFinanceDetails = new FinanceDetails();
        dummyFinanceDetails.setBalance(0);
        dummyFinanceDetails.setOrders(Arrays.asList(dummyOrder));
        
        reg.setFinanceDetails(dummyFinanceDetails);
    }
}
