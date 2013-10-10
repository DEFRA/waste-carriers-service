package uk.gov.ea.wastecarrier.services.health;

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
        mongo.getDatabaseNames();
        return Result.healthy();
    }

}
