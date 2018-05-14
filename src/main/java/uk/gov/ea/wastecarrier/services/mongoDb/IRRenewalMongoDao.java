package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import com.mongodb.DB;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.core.irdata.CompanyIRData;
import uk.gov.ea.wastecarrier.services.core.irdata.IndividualIRData;
import uk.gov.ea.wastecarrier.services.core.irdata.IRData;
import uk.gov.ea.wastecarrier.services.core.irdata.PartnersIRData;
import uk.gov.ea.wastecarrier.services.core.irdata.PublicBodyIRData;

public class IRRenewalMongoDao
{
    /** logger for this class. */
    private static Logger log = Logger.getLogger(IRRenewalMongoDao.class.getName());

    /** The database helper. */
    private DatabaseHelper databaseHelper;

    /**
     * Constructor with arguments
     *
     * @param database the DatabaseConfiguration
     */
    public IRRenewalMongoDao(DatabaseConfiguration database)
    {
        log.fine("Constructing DAO with databaseHelper.");
        this.databaseHelper = new DatabaseHelper(database);
    }

    public IRData findIRData(String registrationNumber)
    {
        log.info("Param GET Method Detected - findIRData limited by registrationNumber");
        DB db = databaseHelper.getConnection();
        if (db != null)
        {
            // Create MONGOJACK connection to the database via different IRData types
            JacksonDBCollection<IRData, String> genericIRrenewalData = JacksonDBCollection.wrap(
                    db.getCollection(IRData.COLLECTION_NAME), IRData.class, String.class);

            // Query to find matching reference number
            DBQuery.Query paramQuery = getMatchingReferenceNumberQuery(registrationNumber);

            // Search for matching registration
            IRData irData = genericIRrenewalData.findOne(paramQuery);

            if (irData != null)
            {
                return irData;
            }

            log.info("Failed to find company with number: " + registrationNumber);
            throw new WebApplicationException(Status.NO_CONTENT);

        }
        // If reached here, no IR data found
        return null;
    }

    public CompanyIRData findOneCompanyIRData(String registrationNumber)
    {
        log.info("Param GET Method Detected - findOneCompanyIRData limited by registrationNumber");
        DB db = databaseHelper.getConnection();
        if (db != null)
        {
            // Create MONGOJACK connection to the database via different IRData types
            JacksonDBCollection<CompanyIRData, String> irRenewalData = JacksonDBCollection.wrap(
                    db.getCollection(IRData.COLLECTION_NAME), CompanyIRData.class, String.class);

            // Query to find matching reference number
            DBQuery.Query paramQuery = getMatchingReferenceNumberQuery(registrationNumber);

            // Search for matching registration
            CompanyIRData irData = irRenewalData.findOne(paramQuery);
            if (irData != null)
            {
                return irData;
            }

            log.info("Failed to find company with number:" + registrationNumber);
            throw new WebApplicationException(Status.NOT_FOUND);

        }
        // If reached here, no IR data found
        return null;
    }

    public IndividualIRData findOneIndividualIRData(String registrationNumber)
    {
        log.info("Param GET Method Detected - findOneIndividualIRData limited by registrationNumber");
        DB db = databaseHelper.getConnection();
        if (db != null)
        {
            // Create MONGOJACK connection to the database via different IRData types
            JacksonDBCollection<IndividualIRData, String> irRenewalData = JacksonDBCollection.wrap(
                    db.getCollection(IRData.COLLECTION_NAME), IndividualIRData.class, String.class);

            // Query to find matching reference number
            DBQuery.Query paramQuery = getMatchingReferenceNumberQuery(registrationNumber);

            // Search for matching registration
            IndividualIRData irData = irRenewalData.findOne(paramQuery);
            if (irData != null)
            {
                return irData;
            }

            log.info("Failed to find company with number:" + registrationNumber);
            throw new WebApplicationException(Status.NOT_FOUND);

        }
        // If reached here, no IR data found
        return null;
    }

    public PartnersIRData findOnePartnersIRData(String registrationNumber)
    {
        log.info("Param GET Method Detected - findOnePartnersIRData limited by registrationNumber");
        DB db = databaseHelper.getConnection();
        if (db != null)
        {
            // Create MONGOJACK connection to the database via different IRData types
            JacksonDBCollection<PartnersIRData, String> irRenewalData = JacksonDBCollection.wrap(
                    db.getCollection(IRData.COLLECTION_NAME), PartnersIRData.class, String.class);

            // Query to find matching reference number
            DBQuery.Query paramQuery = getMatchingReferenceNumberQuery(registrationNumber);

            // Search for matching registration
            PartnersIRData irData = irRenewalData.findOne(paramQuery);
            if (irData != null)
            {
                return irData;
            }

            log.info("Failed to find company with number:" + registrationNumber);
            throw new WebApplicationException(Status.NOT_FOUND);

        }
        // If reached here, no IR data found
        return null;
    }

    public PublicBodyIRData findOnePublicBodyIRData(String registrationNumber)
    {
        log.info("Param GET Method Detected - findOnePublicBodyIRData limited by registrationNumber");
        DB db = databaseHelper.getConnection();
        if (db != null)
        {
            // Create MONGOJACK connection to the database via different IRData types
            JacksonDBCollection<PublicBodyIRData, String> irRenewalData = JacksonDBCollection.wrap(
                    db.getCollection(IRData.COLLECTION_NAME), PublicBodyIRData.class, String.class);

            // Query to find matching reference number
            DBQuery.Query paramQuery = getMatchingReferenceNumberQuery(registrationNumber);

            // Search for matching registration
            PublicBodyIRData irData = irRenewalData.findOne(paramQuery);
            if (irData != null)
            {
                return irData;
            }

            log.info("Failed to find company with number:" + registrationNumber);
            throw new WebApplicationException(Status.NOT_FOUND);

        }
        // If reached here, no IR data found
        return null;
    }

    private DBQuery.Query getMatchingReferenceNumberQuery(String registrationNumber)
    {
        // Query to find matching reference number
        return DBQuery.is("referenceNumber", registrationNumber);
    }

    public void addIRData(IRData irData)
    {
        log.fine("Adding ir data to database for = " + irData.getReferenceNumber());
        DB db = databaseHelper.getConnection();
        if (db != null)
        {
            WriteResult<?, String> result;
            if (irData instanceof CompanyIRData)
            {
                log.fine("Is Company IR DATA");
                // Cast type from generic type
                CompanyIRData companyIRData = (CompanyIRData)irData;

                // Create MONGOJACK connection to the database
                JacksonDBCollection<CompanyIRData, String> renewalData = JacksonDBCollection.wrap(
                        db.getCollection(IRData.COLLECTION_NAME), CompanyIRData.class, String.class);

                // Insert IR data information into database
                result = renewalData.insert(companyIRData);
            }
            else if (irData instanceof IndividualIRData)
            {
                log.fine("Is Individual IR DATA");
                // Cast type from generic type
                IndividualIRData individualIRData = (IndividualIRData)irData;

                // Create MONGOJACK connection to the database
                JacksonDBCollection<IndividualIRData, String> renewalData = JacksonDBCollection.wrap(
                        db.getCollection(IRData.COLLECTION_NAME), IndividualIRData.class, String.class);

                // Insert IR data information into database
                result = renewalData.insert(individualIRData);
            }
            else if (irData instanceof PartnersIRData)
            {
                log.fine("Is Partners IR DATA");
                // Cast type from generic type
                PartnersIRData partnersIRData = (PartnersIRData)irData;

                // Create MONGOJACK connection to the database
                JacksonDBCollection<PartnersIRData, String> renewalData = JacksonDBCollection.wrap(
                        db.getCollection(IRData.COLLECTION_NAME), PartnersIRData.class, String.class);

                // Insert IR data information into database
                result = renewalData.insert(partnersIRData);
            }
            else if (irData instanceof PublicBodyIRData)
            {
                log.fine("Is Public Body IR DATA");
                // Cast type from generic type
                PublicBodyIRData publicBodyIRData = (PublicBodyIRData)irData;

                // Create MONGOJACK connection to the database
                JacksonDBCollection<PublicBodyIRData, String> renewalData = JacksonDBCollection.wrap(
                        db.getCollection(IRData.COLLECTION_NAME), PublicBodyIRData.class, String.class);

                // Insert IR data information into database
                result = renewalData.insert(publicBodyIRData);
            }
            else
            {
                // Create MONGOJACK connection to the database
                JacksonDBCollection<IRData, String> renewalData = JacksonDBCollection.wrap(
                        db.getCollection(IRData.COLLECTION_NAME), IRData.class, String.class);

                // Insert IR data information into database
                result = renewalData.insert(irData);
            }

            // Get unique ID out of response, and find updated record
            String id = result.getSavedId();
            /*
             * Alternative long hand for getting id:
             * String id = result.getDbObject().get("_id").toString();
             */
            log.fine("Added to the database with otherId: " + id);

            IRData resObject = null;
            if (result.getSavedObject() instanceof CompanyIRData)
            {
                resObject = (CompanyIRData) result.getSavedObject();
            }
            else if (result.getSavedObject() instanceof IndividualIRData)
            {
                resObject = (IndividualIRData) result.getSavedObject();
            }
            else if (result.getSavedObject() instanceof PartnersIRData)
            {
                resObject = (PartnersIRData) result.getSavedObject();
            }
            else if (result.getSavedObject() instanceof PublicBodyIRData)
            {
                resObject = (PublicBodyIRData) result.getSavedObject();
            }
            log.fine("Added = " + resObject.getReferenceNumber() + " to the database with id:" + resObject.getId());
        }
        else
        {
            log.severe("Could not establish database connection to MongoDB! Check the database is running");
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }
    }

    public void dropIRData()
    {
        log.fine("Dropping ir data from database");
        DB db = databaseHelper.getConnection();
        if (db != null)
        {
            // Create MONGOJACK connection to the database
            JacksonDBCollection<IRData, String> renewalData = JacksonDBCollection.wrap(
                    db.getCollection(IRData.COLLECTION_NAME), IRData.class, String.class);

            renewalData.drop();
            log.info("Dropped ir data from database");
        }
        else
        {
            log.severe("Could not establish database connection to MongoDB! Check the database is running");
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);
        }

    }

}
