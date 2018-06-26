package uk.gov.ea.wastecarrier.services;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class DatabaseConfiguration {
    @NotEmpty
    @JsonProperty
    private String url;

    private String host;

    private int port;
    
    @NotEmpty
    @JsonProperty
    private String name;
    
    @JsonProperty
    private String username;

    @JsonProperty
    private String password;

    @Min(1000)
    @Max(60000)
    @JsonProperty
    private int serverSelectionTimeout;

    public DatabaseConfiguration() {}

    public DatabaseConfiguration(
            String url,
            String name,
            String username,
            String password,
            int serverSelectionTimeout
    ) {
        this.url = url;
        this.name = name;
        this.username = username;
        this.password = password;
        this.serverSelectionTimeout = serverSelectionTimeout;

        setHostAndPort();
    }

    public String getUrl() {
        return this.url;
    }

    public String getHost() {
        if (this.host == null || this.host.isEmpty()) setHostAndPort();

        return host;
    }

    public int getPort() {
        if (this.port == 0) setHostAndPort();

        return port;
    }
    
    public String getName() {
        return name;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public int getServerSelectionTimeout() {
        return serverSelectionTimeout;
    }

    private void setHostAndPort() {
        String[] parts = this.url.split(":");
        this.host = parts[0];
        this.port = Integer.valueOf(parts[1]);
    }
}
