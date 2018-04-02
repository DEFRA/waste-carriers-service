package uk.gov.ea.wastecarrier.services.health;

import java.util.Set;

import com.mongodb.DB;

import com.yammer.metrics.core.HealthCheck;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.mongoDb.DatabaseHelper;

/**
 * HeathCheck Class for Monitoring the status of the Mongo Database
 * 
 * @author Steve
 *
 */
public class MongoHealthCheck extends HealthCheck {

    private DatabaseHelper databaseHelper;

    public MongoHealthCheck(DatabaseConfiguration database) {
        super("MongoHealthCheck");
        this.databaseHelper = new DatabaseHelper(database);
    }

    @Override
    protected Result check() throws Exception {
    	//A regular non-admin mongo user may not be able to see/list all the database names...
    	DB db = this.databaseHelper.getConnection();
    	Set<String> collectionNames = db.getCollectionNames();
    	collectionNames.isEmpty();
        return Result.healthy();
    }

}
