package uk.gov.ea.wastecarrier.services;

import javax.validation.Valid;

import com.yammer.dropwizard.config.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class IRConfiguration extends Configuration {
	
	@Valid
    @NotEmpty
    @JsonProperty
    private String irRenewalFolderPath = "src/test/resources/ir_data/";
    
    @Valid
    @NotEmpty
    @JsonProperty
    private String irRenewalCompanyFileName = "irdata_company.csv";

    @Valid
    @NotEmpty
    @JsonProperty
    private String irRenewalIndividualFileName = "irdata_individual.csv";
    
    @Valid
    @NotEmpty
    @JsonProperty
    private String irRenewalPartnersFileName = "irdata_partners.csv";
    
    @Valid
    @NotEmpty
    @JsonProperty
    private String irRenewalPublicBodyFileName = "irdata_publicbody.csv";
    
    public String getIrRenewalFolderPath() {
        return irRenewalFolderPath;
    }
    
	public String getIrRenewalCompanyFileName()
	{
		return irRenewalCompanyFileName;
	}

	public String getIrRenewalIndividualFileName()
	{
		return irRenewalIndividualFileName;
	}

	public String getIrRenewalPartnersFileName()
	{
		return irRenewalPartnersFileName;
	}

	public String getIrRenewalPublicBodyFileName()
	{
		return irRenewalPublicBodyFileName;
	}
}
