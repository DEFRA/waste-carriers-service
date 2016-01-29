package uk.gov.ea.wastecarrier.services;

import javax.validation.Valid;

import com.yammer.dropwizard.config.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class IRConfiguration extends Configuration {
	
    @Valid
    @NotEmpty
    @JsonProperty
    private String irFolderPath;
    
    @Valid
    @NotEmpty
    @JsonProperty
    private String irCompanyFileName = "irdata_company.csv";

    @Valid
    @NotEmpty
    @JsonProperty
    private String irIndividualFileName = "irdata_individual.csv";
    
    @Valid
    @NotEmpty
    @JsonProperty
    private String irPartnersFileName = "irdata_partners.csv";
    
    @Valid
    @NotEmpty
    @JsonProperty
    private String irPublicBodyFileName = "irdata_publicbody.csv";
    
    public String getIrFolderPath()
    {
        return irFolderPath;
    }
    
    public String getIrCompanyFileName()
    {
        return irCompanyFileName;
    }

    public String getIrIndividualFileName()
    {
        return irIndividualFileName;
    }

    public String getIrPartnersFileName()
    {
        return irPartnersFileName;
    }

    public String getIrPublicBodyFileName()
    {
        return irPublicBodyFileName;
    }
}
