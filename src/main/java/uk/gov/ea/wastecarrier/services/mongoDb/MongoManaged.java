package uk.gov.ea.wastecarrier.services.mongoDb;

import com.mongodb.MongoClient;
import com.yammer.dropwizard.lifecycle.Managed;

public class MongoManaged implements Managed {

    private MongoClient m;

    public MongoManaged(MongoClient m) {
        this.m = m;
    }

    public void start() throws Exception {
    }

    public void stop() throws Exception {
        m.close();
    }
}
