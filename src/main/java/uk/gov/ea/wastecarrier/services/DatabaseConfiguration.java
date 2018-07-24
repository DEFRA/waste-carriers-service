package uk.gov.ea.wastecarrier.services;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.Mongo;
import com.mongodb.MongoClientURI;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseConfiguration {

    @NotEmpty
    @JsonProperty
    private String uri;

    @Min(1000)
    @Max(60000)
    @JsonProperty
    private int serverSelectionTimeout;

    private MongoClientURI mongoClientURI;

    public DatabaseConfiguration() {}

    public DatabaseConfiguration(String uri, int serverSelectionTimeout) {
        this.uri = uri;
        this.serverSelectionTimeout = serverSelectionTimeout;
    }

    public String getUri() {
        return this.uri;
    }

    public MongoClientURI getMongoClientURI() {

        if (this.mongoClientURI == null) {
            this.mongoClientURI = new MongoClientURI(includeServerSelectionTimeoutMS(uri, serverSelectionTimeout));
        }

        return this.mongoClientURI;
    }

    public int getServerSelectionTimeout() {
        return serverSelectionTimeout;
    }

    private String includeServerSelectionTimeoutMS(String uri, int serverSelectionTimeout) {
        if (uri.contains("?")) {
            return uri + "&serverSelectionTimeoutMS=" + String.valueOf(serverSelectionTimeout);
        }
        else {
            return uri + "?serverSelectionTimeoutMS=" + String.valueOf(serverSelectionTimeout);
        }
    }
}
