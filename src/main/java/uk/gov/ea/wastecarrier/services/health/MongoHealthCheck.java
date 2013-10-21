package uk.gov.ea.wastecarrier.services.health;

import java.util.Set;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.yammer.metrics.core.HealthCheck;

public class MongoHealthCheck extends HealthCheck {

    private MongoClient mongo;

    public MongoHealthCheck(MongoClient mongo) {
        super("MongoHealthCheck");
        this.mongo = mongo;
    }

    @Override
    protected Result check() throws Exception {
    	//A regular non-admin mongo user may not be able to see/list all the database names...
        //mongo.getDatabaseNames();
    	//TODO get database name from configuration rather than hard-coding again here. Or: Allow the mongoUser to see/read all databases.
    	DB db = mongo.getDB("waste-carriers");
    	Set<String> collectionNames = db.getCollectionNames();
    	collectionNames.isEmpty();
        return Result.healthy();
    }

}
