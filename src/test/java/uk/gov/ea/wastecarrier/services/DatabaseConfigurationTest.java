package uk.gov.ea.wastecarrier.services;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DatabaseConfigurationTest {

    @Test
    public void initializeWithLocalUri() {
        DatabaseConfiguration dbConfig = new DatabaseConfiguration(
                "mongodb://mongoUser:password1234@localhost:27017/waste-carriers",
                1000
        );

        assertEquals("waste-carriers", dbConfig.getMongoClientURI().getDatabase());
        assertEquals("mongoUser", dbConfig.getMongoClientURI().getUsername());
    }

    @Test
    public void initializeUriIncludesServerSelectionTimeout() {
        DatabaseConfiguration dbConfig = new DatabaseConfiguration(
                "mongodb://mongoUser:password1234@localhost:27017/waste-carriers",
                1000
        );

        assertEquals(
                "mongodb://mongoUser:password1234@localhost:27017/waste-carriers?serverSelectionTimeoutMS=1000",
                dbConfig.getMongoClientURI().getURI()
        );
        assertEquals(1000, dbConfig.getMongoClientURI().getOptions().getServerSelectionTimeout());
    }

    @Test
    public void initializeWithProductionUri() {
        DatabaseConfiguration dbConfig = new DatabaseConfiguration(
                "mongodb://mongoUser:password1234@mongodb1:27017,mongodb2:27017,mongodb3:27017/waste-carriers?replicaSet=wcrepl",
                1000
        );

        assertEquals("waste-carriers", dbConfig.getMongoClientURI().getDatabase());
        assertEquals("mongoUser", dbConfig.getMongoClientURI().getUsername());
    }
}
