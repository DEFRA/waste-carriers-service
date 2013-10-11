package uk.gov.ea.wastecarrier.services;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class DatabaseConfiguration {
    @NotEmpty
    @JsonProperty
    private String host;

    @Min(1)
    @Max(65535)
    @JsonProperty
    private int port = 5672;
    
    @NotEmpty
    @JsonProperty
    private String name;
    
    @JsonProperty
    private String username;

	@JsonProperty
    private String password;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
    
    public String getName() {
        return name;
    }
    
    /**
	 * @return the username
	 */
	public String getUsername()
	{
		return username;
	}

	/**
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}
}
