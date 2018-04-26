package uk.gov.ea.wastecarrier.services.tasks;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.IRConfiguration;
import uk.gov.ea.wastecarrier.services.core.irdata.*;
import uk.gov.ea.wastecarrier.services.mongoDb.IRRenewalMongoDao;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * The IR Renewal Populator creates records in the mongoDB database from exported data from the legacy IR system.
 * That data can subsequently be queried and used to populate some registration data.
 *
 * To use this service call, E.g.
 * curl -X POST http://localhost:9091/tasks/ir-repopulate
 * Which performs a full re-population for entries in the csv's provided
 *
 * @author Steve
 *
 */
public class IRRenewalPopulator extends Task
{

    private ArrayList<IRData> irDataList;
    private IRRenewalMongoDao dao;
    private String irCompanyDataFilePath;
    private String irIndividualDataFilePath;
    private String irPartnersDataFilePath;
    private String irPublicBodyDataFilePath;
    private static Logger log = Logger.getLogger(IRRenewalPopulator.class.getName());
    
    public enum IRRenewal_Type {
        Company,
        Individual,
        Partner,
        PublicBody
    }

    public IRRenewalPopulator(String name, DatabaseConfiguration database, IRConfiguration irRenewalConfig)
    {
        super(name);
        this.dao = new IRRenewalMongoDao(database);
        this.irCompanyDataFilePath = irRenewalConfig.getIrFolderPath()
                + File.separatorChar
                + irRenewalConfig.getIrCompanyFileName();
        this.irIndividualDataFilePath = irRenewalConfig.getIrFolderPath()
                + File.separatorChar
                + irRenewalConfig.getIrIndividualFileName();
        this.irPartnersDataFilePath = irRenewalConfig.getIrFolderPath()
                + File.separatorChar
                + irRenewalConfig.getIrPartnersFileName();
        this.irPublicBodyDataFilePath = irRenewalConfig.getIrFolderPath()
                + File.separatorChar
                + irRenewalConfig.getIrPublicBodyFileName();
        this.irDataList = new ArrayList<IRData>();
        log.fine("IR data populator loaded");
    }
    
    /**
     * Performs the operation Used Via the administration ports to recreate the full IR renewal data
     * in the mongo database. It first drops all existing data, then parses the 4 different CSV files
     * and puts each row found in the database, (excluding header rows)
     *
     * Usage:
     * curl -X POST http://[SERVER]:[ADMINPORT]/tasks/[this.getName()]
     */
    @Override
    public void execute(ImmutableMultimap<String, String> arg0, PrintWriter out) throws Exception
    {
        // Drop any existing IR renewal data
        dropAllIRData();
        
        // Populate list with initial set of test data
        populateListwithTestIRData();
        
        // Setup IR data with list in CSV files
        populateIRData(this.irCompanyDataFilePath, IRRenewal_Type.Company);
        populateIRData(this.irIndividualDataFilePath, IRRenewal_Type.Individual);
        populateIRData(this.irPartnersDataFilePath, IRRenewal_Type.Partner);
        populateIRData(this.irPublicBodyDataFilePath, IRRenewal_Type.PublicBody);
        
        // Populate database with data found in list
        populateDBwithIRData();
    }
    
    private void dropAllIRData()
    {
        // Drop any existing IR renewal data
        dao.dropIRData();
        // Remove data list from memory
        irDataList.clear();
    }
    
    private void populateDBwithIRData()
    {
        // Populate database with data found in list
        for (IRData irData : irDataList)
        {
            dao.addIRData(irData);
        }
        log.info("Added " + irDataList.size() + " IR records to the database.");
    }

    /**
     * Creates a lookup for the IR renewal data based on a CSV Files provided
     *
     * @param csvFile a string of the path to the CSV file
     * @param irType IRRenewal_Type is the type of IR data provided, as each type has unique data entry
     */
    private void populateIRData(String csvFile, IRRenewal_Type irType)
    {
        String[] irDataRow;
        CSVReader reader = null;
        try {
            int count = 0;
            reader = new CSVReader(new FileReader(csvFile));
            while ((irDataRow = reader.readNext()) != null) {
                // use comma as separator
                if (irDataRow.length < 4) {
                    continue;
                }
                log.fine("IR Data [0: " + irDataRow[0] +
                        " , 1: " + irDataRow[1] +
                        " , 2: "+ irDataRow[2] +
                        " , 3: " + irDataRow[3] + "]");

                if ("CB_REFERENCE_NUMBER".equalsIgnoreCase(irDataRow[0]))
                {
                    // skip this iteration, to ignore CSV headings
                    continue;
                }
                count++;
                
                // Updated method
                switch (irType)
                {
                case Company:
                    //
                    this.irDataList.add(new CompanyIRData(irDataRow));
                    break;
                case Individual:
                    //
                    this.irDataList.add(new IndividualIRData(irDataRow));
                    break;
                case Partner:
                    //
                    this.irDataList.add(new PartnersIRData(irDataRow));
                    break;
                case PublicBody:
                    //
                    this.irDataList.add(new PublicBodyIRData(irDataRow));
                    break;
                default:
                    break;
                }
            }
            log.info("Read: " + count + " irrenewals from " + csvFile + " file.");
            if (count == 0)
            {
                log.severe("Error: Could not find any irdata");
            }
            
        }
        catch (FileNotFoundException e)
        {
            log.severe("File not Found: " + e.getMessage());
        }
        catch (IOException e)
        {
            log.severe("IO Exception: " + e.getMessage());
        }
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
                log.severe("IOException while closing CSV reader: "
                        + e.getMessage());
            }
        }
        log.fine("Done");
    }
    
    /**
     * Loads sample data for the purpose of populating the IR data with a minimal set of data for the tests
     * to be able to use and find.
     *
     */
    private void populateListwithTestIRData()
    {
        long ninetyDaysMillis = 90L * 24L * 60L * 60L * 1000L;
        Date futureDate = new Date(System.currentTimeMillis() + ninetyDaysMillis);
        Date expiredDate = new Date(System.currentTimeMillis() - ninetyDaysMillis);
        SimpleDateFormat myDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        
        String testCompanyData[] = {"CB/AE9999XX/A001",
                myDateFormat.format(futureDate),
                "Carrier and Broker",
                "Company",
                "IR Company Name",
                "",
                "10926928"};
        this.irDataList.add(new CompanyIRData(testCompanyData));

        String testIndividualData[] = {"CB/AN9999YY/R002",
                myDateFormat.format(futureDate),
                "Carrier",
                "Person",
                "Joe Grades",
                "Sole Trader Ltd",
                "4/23/1959"};
        this.irDataList.add(new IndividualIRData(testIndividualData));
        
        String expiredIndividualData[] = {"CB/AN9999YY/R999",
                myDateFormat.format(expiredDate),
                "Carrier",
                "Person",
                "Joe Grades",
                "Expired Sole Trader Ltd",
                "4/23/1959"};
        this.irDataList.add(new IndividualIRData(expiredIndividualData));

        String testPartnerData[] = {"CB/AN9999ZZ/R002",
                myDateFormat.format(futureDate),
                "Carrier and Broker",
                "Organisation of Individuals",
                "Partnership",
                "Joe Grades",
                "",
                "8/15/1979"};
        this.irDataList.add(new PartnersIRData(testPartnerData));

        String testPartnerData2[] = {"CB/AN9999ZZ/R003",
                myDateFormat.format(futureDate),
                "Carrier and Broker",
                "Organisation of Individuals",
                "Partnership",
                "IR Company Name abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 .-&'[],()",
                "",
                "8/15/1979"};
        this.irDataList.add(new PartnersIRData(testPartnerData2));

        String testPublicBodyData[] = {"CB/VM9999WW/A001",
                myDateFormat.format(futureDate),
                "Carrier",
                "Public Body",
                "Public Body Council",
                "Public Body Name"};
        this.irDataList.add(new PublicBodyIRData(testPublicBodyData));
        
        log.info("Read " + irDataList.size() + " Test IR records.");
    }
    
}
