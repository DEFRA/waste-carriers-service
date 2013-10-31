package uk.gov.ea.wastecarrier.services.core;

import java.util.logging.Logger;

import javax.validation.Valid;

import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.ObjectId;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents the entire model of the initial Waste Carrier Registration details
 * Ideally the models details could be split into subclasses for each distinct type of details (page)
 *
 */
public class Registration
{
	/*
	 * This is the Key for the Registration details
	 */
	@JsonProperty 
	@Id
	@ObjectId
    private String id;

	/* 
	 * These are the Business Details
	 */
	@Valid
	@NotEmpty
	@JsonProperty
	private String businessType;
	@JsonProperty
	private String companyName;
	@JsonProperty
	private String individualsType;
	@JsonProperty
	private String publicBodyType;
	@JsonProperty
	private String publicBodyTypeOther;

	/* 
	 * These are the Trading Address Details
	 */
	@JsonProperty
	private String houseNumber;
	@JsonProperty
	private String streetLine1;
	@JsonProperty
	private String streetLine2;
	@JsonProperty
	private String townCity;
	@JsonProperty
	private String postcode;
	
	/**
	 * TODO: Determine if still need address added from rails?
	 */
	@JsonProperty
	private String address;
	
	/**
	 * TODO: Determine if still need UPRN? added from rails?
	 */
	@JsonProperty
	private String uprn;

	/* 
	 * These are the Contact Details
	 */
	@NotEmpty
	@JsonProperty
	private String title;
	@JsonProperty
	private String otherTitle;
	@NotEmpty
	@JsonProperty
	private String firstName;
	@NotEmpty
	@JsonProperty
	private String lastName;
	@JsonProperty
	private String position;

	@NotEmpty
	@JsonProperty
	private String phoneNumber;
	@NotEmpty
	@JsonProperty
	private String email;
	
	/* 
	 * These are the Declaration Details
	 */
	@NotEmpty
	@JsonProperty
	private String declaration;
	
	@JsonInclude(Include.NON_DEFAULT)
	private String regIdentifier;

	@JsonProperty
	private String status;
	
	/* Static not applicable value */
	//private final static String NA = "n/a";
	

	public final static String COLLECTION_SINGULAR_NAME = "registration";
	public final static String COLLECTION_NAME = COLLECTION_SINGULAR_NAME +"s";
	public final static String COUNTERS_COLLECTION_NAME = "counters";
	
	public final static String REGID_PREFIX = "CBD";
	public final static int REGID_LENGTH = 10;
	
	@JsonInclude(Include.NON_DEFAULT)	/*TEST: This should not be generated in the JSON */
	private MetaData metaData;
	
	// Standard logging declaration
	private Logger log = Logger.getLogger(Registration.class.getName());
	
	/**
	 * Default constructor is required for non complete objects, and as alternative to 
	 * defining specific JSON names per property
	 */
	public Registration ()
	{
	}
	
	/*
	 * Alternative to empty constructor
	 * 
	 * public Registration (@JsonProperty("registerAs") String registerAs, @JsonProperty("businessType") String businessType, 
			@JsonProperty("companyName") String companyName, @JsonProperty("organisationType") String organisationType, 
			@JsonProperty("companyNumber") String companyNumber, @JsonProperty("publicBodyType") String publicBodyType,
			@JsonProperty("houseNumber") String houseNumber, @JsonProperty("postcode") String postcode,
			@JsonProperty("address") String address, @JsonProperty("title") String title,
			@JsonProperty("firstName") String firstName, @JsonProperty("lastName") String lastName,
			@JsonProperty("phoneNumber") String phoneNumber, @JsonProperty("email") String email,
			@JsonProperty("confirmDeclaration") String confirmDeclaration)
	*
	* OR
	*
	public Registration (String id, String registerAs, String businessType, 
			String companyName, String organisationType, 
			String companyNumber, String publicBodyType,
			String houseNumber, String postcode,
			String address, String title,
			String firstName, String lastName,
			String phoneNumber, String email,
			String confirmDeclaration)
	{
		this.id = id;
		this.registerAs = registerAs;
		this.businessType = businessType;
		this.companyName = companyName;
		this.organisationType = organisationType;
		this.companyNumber = companyNumber;
		this.publicBodyType = publicBodyType;
		
		this.houseNumber = houseNumber;
		this.postcode = postcode;
		this.address = address;
		
		this.title = title;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
		this.email = email;
		
		this.confirmDeclaration = confirmDeclaration;
	}*/
	
	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return the businessType
	 */
	public String getBusinessType()
	{
		return businessType;
	}

	/**
	 * @return the companyName
	 */
	public String getCompanyName()
	{
		return companyName;
	}

	/**
	 * @return the organisationType
	 */
	public String getIndividualsType()
	{
		return individualsType;
	}

	/**
	 * @return the publicBodyType
	 */
	public String getPublicBodyType()
	{
		return publicBodyType;
	}
	
	/**
	 * @return the publicBodyTypeOther
	 */
	public String getPublicBodyTypeOther()
	{
		return publicBodyTypeOther;
	}

	/**
	 * @return the houseNumber
	 */
	public String getHouseNumber()
	{
		return houseNumber;
	}
	
	/**
	 * @return the streetLineOne
	 */
	public String getStreetLine1()
	{
		return streetLine1;
	}

	/**
	 * @return the streetLineTwo
	 */
	public String getStreetLine2()
	{
		return streetLine2;
	}

	/**
	 * @return the townCity
	 */
	public String getTownCity()
	{
		return townCity;
	}

	/**
	 * @return the postcode
	 */
	public String getPostcode()
	{
		return postcode;
	}
	
	/**
	 * @return the address
	 */
	public String getAddress()
	{
		return address;
	}
	
	/**
	 * @return the uprn
	 */
	public String getUprn()
	{
		return uprn;
	}

	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName()
	{
		return firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName()
	{
		return lastName;
	}

	/**
	 * @return the phoneNumber
	 */
	public String getPhoneNumber()
	{
		return phoneNumber;
	}

	/**
	 * @return the email
	 */
	public String getEmail()
	{
		return email;
	}

	/**
	 * @return the confrimDeclaration
	 */
	public String getDeclaration()
	{
		return declaration;
	}
	
	/**
	 * @return the regIdentifier
	 */
	public String getRegIdentifier()
	{
		return regIdentifier;
	}
	
	/**
	 * @return the metaData
	 */
	public MetaData getMetaData()
	{
		return metaData;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * @param businessType the businessType to set
	 */
	public void setBusinessType(String businessType)
	{
		this.businessType = businessType;
	}

	/**
	 * @param companyName the companyName to set
	 */
	public void setCompanyName(String companyName)
	{
		this.companyName = companyName;
	}

	/**
	 * @param organisationType the organisationType to set
	 */
	public void setIndividualsType(String organisationType)
	{
		this.individualsType = organisationType;
	}

	/**
	 * @param publicBodyType the publicBodyType to set
	 */
	public void setPublicBodyType(String publicBodyType)
	{
		this.publicBodyType = publicBodyType;
	}
	
	/**
	 * @param publicBodyTypeOther the publicBodyTypeOther to set
	 */
	public void setPublicBodyTypeOther(String publicBodyTypeOther)
	{
		this.publicBodyTypeOther = publicBodyTypeOther;
	}

	/**
	 * @param houseNumber the houseNumber to set
	 */
	public void setHouseNumber(String houseNumber)
	{
		this.houseNumber = houseNumber;
	}
	
	/**
	 * @param streetLineOne the streetLineOne to set
	 */
	public void setStreetLine1(String streetLineOne)
	{
		this.streetLine1 = streetLineOne;
	}
	
	/**
	 * @param streetLineTwo the streetLineTwo to set
	 */
	public void setStreetLine2(String streetLineTwo)
	{
		this.streetLine2 = streetLineTwo;
	}
	
	/**
	 * @param townCity the townCity to set
	 */
	public void setTownCity(String townCity)
	{
		this.townCity = townCity;
	}

	/**
	 * @param postcode the postcode to set
	 */
	public void setPostcode(String postcode)
	{
		this.postcode = postcode;
	}
	
	/**
	 * @param address the address to set
	 */
	public void setAddress(String address)
	{
		this.address = address;
	}
	
	/**
	 * @param uprn the uprn to set
	 */
	public void setUprn(String uprn)
	{
		this.uprn = uprn;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	/**
	 * @param phoneNumber the phoneNumber to set
	 */
	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email)
	{
		this.email = email;
	}

	/**
	 * @param confirmDeclaration the confirmDeclaration to set
	 */
	public void setDeclaration(String declaration)
	{
		this.declaration = declaration;
	}

	public String getOtherTitle() {
		return otherTitle;
	}

	public void setOtherTitle(String otherTitle) {
		this.otherTitle = otherTitle;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}
	
	/**
	 * @param regIdentifier the regIdentifier to set
	 */
	public void setRegIdentifier(String regIdentifier)
	{
		this.regIdentifier = regIdentifier;
	}

	/**
	 * @param metaData the metaData to set
	 */
	public void setMetaData(MetaData metaData)
	{
		this.metaData = metaData;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * Custom comparison method for comparing the contents of the user entered fields 
	 * to ensure objects are the same.
	 * 
	 * Effectively created to enable automated testing of comparison objects.
	 */
	@Override
    public boolean equals(Object obj) {
		log.finer("Start equals()");
        if (this == obj) 
        {
            return true;
        }
        if(obj == null || getClass() != obj.getClass()) 
        {
            return false;
        }
        boolean res = true; // default to true, and if any field is not null, reset to false
        res = checkString(this.getBusinessType(), ((Registration) obj).getBusinessType(), res);
        res = checkString(this.getCompanyName(), ((Registration) obj).getCompanyName(), res);
        res = checkString(this.getIndividualsType(), ((Registration) obj).getIndividualsType(), res);
        res = checkString(this.getPublicBodyType(), ((Registration) obj).getPublicBodyType(), res);
        res = checkString(this.getPublicBodyTypeOther(), ((Registration) obj).getPublicBodyTypeOther(), res);
        res = checkString(this.getHouseNumber(), ((Registration) obj).getHouseNumber(), res);
        res = checkString(this.getStreetLine1(), ((Registration) obj).getStreetLine1(), res);
        res = checkString(this.getStreetLine2(), ((Registration) obj).getStreetLine2(), res);
        res = checkString(this.getTownCity(), ((Registration) obj).getTownCity(), res);
        res = checkString(this.getPostcode(), ((Registration) obj).getPostcode(), res);
        res = checkString(this.getAddress(), ((Registration) obj).getAddress(), res);
        res = checkString(this.getUprn(), ((Registration) obj).getUprn(), res);
        res = checkString(this.getTitle(), ((Registration) obj).getTitle(), res);
        res = checkString(this.getFirstName(), ((Registration) obj).getFirstName(), res);
        res = checkString(this.getLastName(), ((Registration) obj).getLastName(), res);
        res = checkString(this.getPhoneNumber(), ((Registration) obj).getPhoneNumber(), res);
        res = checkString(this.getEmail(), ((Registration) obj).getEmail(), res);
        res = checkString(this.getDeclaration(), ((Registration) obj).getDeclaration(), res);
        log.finer("equals result: " + res);
        return res;
    }
	
	/**
	 * Added a hashCode function, as the equals was overridden, and standards say this should 
	 * also be overridden, but this should not be being used.
	 */
	@Override
	public int hashCode()
	{
		assert false : "hashcode not designed";
		return 42; // any arbitrary constant will do
	}
	
	/**
	 * Helper method for comparing strings, and returning status boolean
	 * @param val1
	 * @param val2
	 * @param validSoFar
	 * @return
	 */
	private boolean checkString(String val1, String val2, boolean validSoFar)
	{
		if (val1 != null && validSoFar)
	    {
			validSoFar = false;
	    	if (val1.compareTo(val2) == 0)
	    	{
	    		validSoFar = true;
	    	}
	    }
		return validSoFar;
	}

}
