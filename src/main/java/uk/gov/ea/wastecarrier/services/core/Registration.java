package uk.gov.ea.wastecarrier.services.core;

import java.util.logging.Logger;

import javax.validation.Valid;

import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.ObjectId;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents the entire model of the initial Waste Carrier Registration details
 * Ideally the models details could be split into subclasses for each distinct type of details (page)
 *
 */
/**
 * @author alancruikshanks
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Registration
{
	/**
	 * This is the Key for the Registration details
	 */
	@JsonProperty 
	@Id
	@ObjectId
    private String id;
	
	
	@JsonProperty
	private String registrationType;
	
	/*
	 * These are the recorded smart answers
	 */
	@Valid
	@NotEmpty
	@JsonProperty
	private String businessType;
	@JsonProperty
	private String otherBusinesses;
	@JsonProperty
	private String isMainService;
	@JsonProperty
	private String constructionWaste;
	@JsonProperty
	private String onlyAMF;

	/* 
	 * These are the Business Details
	 */
	@JsonProperty
	private String companyName;
	@JsonProperty
	private String individualsType;
	@JsonProperty
	private String publicBodyType;
	@JsonProperty
	private String publicBodyTypeOther;
	@JsonProperty
	private String companyNo;
	@JsonProperty
	private String companyHouseNo;

	/* 
	 * These are the Trading Address Details
	 */
	@JsonProperty
	private String addressMode;
	@JsonProperty
	private String houseNumber;
	@JsonProperty
	private String streetLine1;
	@JsonProperty
	private String streetLine2;
	@JsonProperty
	private String streetLine3;
	@JsonProperty
	private String streetLine4;
	@JsonProperty
	private String townCity;
	@JsonProperty
	private String postcode;
	@JsonProperty
	private String country;

	@JsonProperty
	private String easting;

	@JsonProperty
	private String northing;

	@JsonProperty
	private String dependentLocality;

	@JsonProperty
	private String dependentThroughfare;

	@JsonProperty
	private String administrativeArea;
	
	@JsonProperty
	private String localAuthorityUpdateDate;

	@JsonProperty
	private String royalMailUpdateDate;
	
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
	//TODO - Temporarily making the title property optional
	//@NotEmpty
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
	@JsonProperty
	private String contactEmail;
	@JsonProperty
	private String altFirstName;
	@JsonProperty
	private String altLastName;
	@JsonProperty
	private String altJobTitle;
	@JsonProperty
	private String altTelephoneNumber;
	@JsonProperty
	private String altEmailAddress;
	@JsonProperty
	private String primaryFirstName;
	@JsonProperty
	private String primaryLastName;
	@JsonProperty
	private String primaryJobTitle;
	@JsonProperty
	private String primaryTelephoneNumber;
	@JsonProperty
	private String primaryEmailAddress;
	
	/*
	 * Payment values
	 */
	@JsonProperty
	private String totalFee;
	@JsonProperty
	private String registrationFee;
	@JsonProperty
	private String copyCardFee;
	@JsonProperty
	private String copyCards;
	
	@JsonProperty
	private String accountEmail;
	
	/* 
	 * These are the Declaration Details
	 */
	@NotEmpty
	@JsonProperty
	private String declaration;
	
	@JsonInclude(Include.NON_DEFAULT)
	private String regIdentifier;

	/*
	 * Random access code - generated for Assisted Digital users
	 */
	@JsonProperty
	private String accessCode;
	
	/* Static not applicable value */
	//private final static String NA = "n/a";
	

	public final static String COLLECTION_SINGULAR_NAME = "registration";
	public final static String COLLECTION_NAME = COLLECTION_SINGULAR_NAME +"s";
	public final static String COUNTERS_COLLECTION_NAME = "counters";
	
	public final static String REGID_PREFIX = "CBD";
	public final static int REGID_LENGTH = 10;
	public final static String REGID_PREFIX_LOWER = "L";
	public final static String REGID_PREFIX_UPPER = "U";
	
	@JsonInclude(Include.NON_DEFAULT)	/*TEST: This should not be generated in the JSON */
	private MetaData metaData;
	
	@JsonInclude(Include.NON_DEFAULT)	/*TEST: This should not be generated in the JSON */
	private Location location;
	
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
	
	public String getRegistrationType() {
		return this.registrationType;
	}

	/**
	 * @return the businessType
	 */
	public String getBusinessType()
	{
		return businessType;
	}
	
	/**
	 * @return the otherBusinesses
	 */
	public String getOtherBusinesses() {
		return otherBusinesses;
	}

	/**
	 * @return the isMainService
	 */
	public String getIsMainService() {
		return isMainService;
	}

	/**
	 * @return the constructionsWaste
	 */
	public String getConstructionWaste() {
		return constructionWaste;
	}

	/**
	 * @return the onlyAMF
	 */
	public String getOnlyAMF() {
		return onlyAMF;
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
	
	public String getCompanyNo() {
		return this.companyNo;
	}

	public String getCompanyHouseNo() {
		return this.companyHouseNo;
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
	
	public String getOtherTitle() {
		return otherTitle;
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
	
	public String getPosition() {
		return position;
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
	public String getContactEmail()
	{
		return contactEmail;
	}
	
	public String getAltFirstName() {
		return this.altFirstName;
	}

	public String getAltLastName() {
		return this.altLastName;
	}

	public String getAltJobTitle() {
		return this.altJobTitle;
	}

	public String getAltTelephoneNumber() {
		return this.altTelephoneNumber;
	}

	public String getAltEmailAddress() {
		return this.altEmailAddress;
	}

	public String getPrimaryFirstName() {
		return this.primaryFirstName;
	}

	public String getPrimaryLastName() {
		return this.primaryLastName;
	}

	public String getPrimaryJobTitle() {
		return this.primaryJobTitle;
	}

	public String getPrimaryTelephoneNumber() {
		return this.primaryTelephoneNumber;
	}
	
	public String getPrimaryEmailAddress() {
		return this.primaryEmailAddress;
	}

	public String getTotalFee() {
		return this.totalFee;
	}

	public String getRegistrationFee() {
		return this.registrationFee;
	}

	public String getCopyCardFee() {
		return this.copyCardFee;
	}

	public String getCopyCards() {
		return this.copyCards;
	}
	
	/**
	 * @return the accountEmail
	 */
	public String getAccountEmail()
	{
		return accountEmail;
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
	 * @return the location
	 */
	public Location getLocation()
	{
		return location;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(String id)
	{
		this.id = id;
	}
	
	public void setRegistrationType(String registrationType) {
		this.registrationType = registrationType;
	}

	/**
	 * @param businessType the businessType to set
	 */
	public void setBusinessType(String businessType)
	{
		this.businessType = businessType;
	}
	
	/**
	 * @param otherBusinesses
	 */
	public void setOtherBusinesses(String otherBusinesses) {
		this.otherBusinesses = otherBusinesses;
	}
	
	/**
	 * @param isMainService
	 */
	public void setIsMainService(String isMainService) {
		this.isMainService = isMainService;
	}
	
	/**
	 * @param constructionWaste
	 */
	public void setConstructionWaste(String constructionWaste) {
		this.constructionWaste = constructionWaste;
	}
	
	/**
	 * @param onlyAMF
	 */
	public void setOnlyAMF(String onlyAMF) {
		this.onlyAMF = onlyAMF;
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
	
	public void setCompanyNo(String companyNo) {
		this.companyNo = companyNo;
	}

	public void setCompanyHouseNo(String companyHouseNo) {
		this.companyHouseNo = companyHouseNo;
	}

	public String getAddressMode() {
		return addressMode;
	}

	public void setAddressMode(String addressMode) {
		this.addressMode = addressMode;
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
	
	public String getStreetLine3() {
		return streetLine3;
	}

	public void setStreetLine3(String streetLine3) {
		this.streetLine3 = streetLine3;
	}

	public String getStreetLine4() {
		return streetLine4;
	}

	public void setStreetLine4(String streetLine4) {
		this.streetLine4 = streetLine4;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
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
	public void setContactEmail(String contactEmail)
	{
		this.contactEmail = contactEmail;
	}
	
	public void setAltFirstName(String altFirstName) {
		this.altFirstName = altFirstName;
	}

	public void setAltLastName(String altLastName) {
		this.altLastName = altLastName;
	}
	
	public void setAltJobTitle(String altJobTitle) {
		this.altJobTitle = altJobTitle;
	}
	
	public void setAltTelephoneNumber(String altTelephoneNumber) {
		this.altTelephoneNumber = altTelephoneNumber;
	}
	
	public void setAltEmailAddress(String altEmailAddress) {
		this.altEmailAddress = altEmailAddress;
	}
	
	public void setPrimaryFirstName(String primaryFirstName) {
		this.primaryFirstName = primaryFirstName;
	}
	
	public void setPrimaryLastName(String primaryLastName) {
		this.primaryLastName = primaryLastName;
	}
	
	public void setPrimaryJobTitle(String primaryJobTitle) {
		this.primaryJobTitle = primaryJobTitle;
	}
	
	public void setPrimaryTelephoneNumber(String primaryTelephoneNumber) {
		this.primaryTelephoneNumber = primaryTelephoneNumber;
	}
	
	public void setPrimaryEmailAddress(String primaryEmailAddress) {
		this.primaryEmailAddress = primaryEmailAddress;
	}
	
	public void setTotalFee(String totalFee) {
		this.totalFee = totalFee;
	}
	
	public void setRegistrationFee(String registrationFee) {
		this.registrationFee = registrationFee;
	}
	
	public void setCopyCardFee(String copyCardFee) {
		this.copyCardFee = copyCardFee;
	}
	
	public void setCopyCards(String copyCards) {
		this.copyCards = copyCards;
	}

	/**
	 * @param accountEmail the accountEmail to set
	 */
	public void setAccountEmail(String accountEmail)
	{
		this.accountEmail = accountEmail;
	}

	/**
	 * @param confirmDeclaration the confirmDeclaration to set
	 */
	public void setDeclaration(String declaration)
	{
		this.declaration = declaration;
	}

	public void setOtherTitle(String otherTitle) {
		this.otherTitle = otherTitle;
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

	public String getAccessCode() {
		return accessCode;
	}

	public void setAccessCode(String accessCode) {
		this.accessCode = accessCode;
	}

	/**
	 * @param metaData the metaData to set
	 */
	public void setMetaData(MetaData metaData)
	{
		this.metaData = metaData;
	}
	
	public String getEasting() {
		return easting;
	}

	public void setEasting(String easting) {
		this.easting = easting;
	}

	public String getNorthing() {
		return northing;
	}

	public void setNorthing(String northing) {
		this.northing = northing;
	}

	public String getDependentLocality() {
		return dependentLocality;
	}

	public void setDependentLocality(String dependentLocality) {
		this.dependentLocality = dependentLocality;
	}

	public String getDependentThroughfare() {
		return dependentThroughfare;
	}

	public void setDependentThroughfare(String dependentThroughfare) {
		this.dependentThroughfare = dependentThroughfare;
	}

	public String getAdministrativeArea() {
		return administrativeArea;
	}

	public void setAdministrativeArea(String administrativeArea) {
		this.administrativeArea = administrativeArea;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location)
	{
		this.location = location;
	}
	
	public String getLocalAuthorityUpdateDate() {
		return localAuthorityUpdateDate;
	}

	public void setLocalAuthorityUpdateDate(String localAuthorityUpdateDate) {
		this.localAuthorityUpdateDate = localAuthorityUpdateDate;
	}

	public String getRoyalMailUpdateDate() {
		return royalMailUpdateDate;
	}

	public void setRoyalMailUpdateDate(String royalMailUpdateDate) {
		this.royalMailUpdateDate = royalMailUpdateDate;
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
        res = checkString(this.getOtherBusinesses(), ((Registration) obj).getOtherBusinesses(), res);
        res = checkString(this.getIsMainService(), ((Registration) obj).getIsMainService(), res);
        res = checkString(this.getConstructionWaste(), ((Registration) obj).getConstructionWaste(), res);
        res = checkString(this.getOnlyAMF(), ((Registration) obj).getOnlyAMF(), res);
        res = checkString(this.getCompanyName(), ((Registration) obj).getCompanyName(), res);
        res = checkString(this.getIndividualsType(), ((Registration) obj).getIndividualsType(), res);
        res = checkString(this.getPublicBodyType(), ((Registration) obj).getPublicBodyType(), res);
        res = checkString(this.getPublicBodyTypeOther(), ((Registration) obj).getPublicBodyTypeOther(), res);
        res = checkString(this.getHouseNumber(), ((Registration) obj).getHouseNumber(), res);
        res = checkString(this.getStreetLine1(), ((Registration) obj).getStreetLine1(), res);
        res = checkString(this.getStreetLine2(), ((Registration) obj).getStreetLine2(), res);
        res = checkString(this.getStreetLine3(), ((Registration) obj).getStreetLine3(), res);
        res = checkString(this.getStreetLine4(), ((Registration) obj).getStreetLine4(), res);
        res = checkString(this.getTownCity(), ((Registration) obj).getTownCity(), res);
        res = checkString(this.getPostcode(), ((Registration) obj).getPostcode(), res);
        res = checkString(this.getCountry(), ((Registration) obj).getCountry(), res);
        res = checkString(this.getAddress(), ((Registration) obj).getAddress(), res);
        res = checkString(this.getUprn(), ((Registration) obj).getUprn(), res);
        res = checkString(this.getTitle(), ((Registration) obj).getTitle(), res);
        res = checkString(this.getOtherTitle(), ((Registration) obj).getOtherTitle(), res);
        res = checkString(this.getFirstName(), ((Registration) obj).getFirstName(), res);
        res = checkString(this.getLastName(), ((Registration) obj).getLastName(), res);
        res = checkString(this.getPosition(), ((Registration) obj).getPosition(), res);
        res = checkString(this.getPhoneNumber(), ((Registration) obj).getPhoneNumber(), res);
        res = checkString(this.getContactEmail(), ((Registration) obj).getContactEmail(), res);
        res = checkString(this.getAltFirstName(), ((Registration) obj).getAltFirstName(), res);
        res = checkString(this.getAltLastName(), ((Registration) obj).getAltLastName(), res);
        res = checkString(this.getAltJobTitle(), ((Registration) obj).getAltJobTitle(), res);
        res = checkString(this.getAltTelephoneNumber(), ((Registration) obj).getAltTelephoneNumber(), res);
        res = checkString(this.getAltEmailAddress(), ((Registration) obj).getAltEmailAddress(), res);
        res = checkString(this.getPrimaryFirstName(), ((Registration) obj).getPrimaryFirstName(), res);
        res = checkString(this.getPrimaryLastName(), ((Registration) obj).getPrimaryLastName(), res);
        res = checkString(this.getPrimaryJobTitle(), ((Registration) obj).getPrimaryJobTitle(), res);
        res = checkString(this.getPrimaryTelephoneNumber(), ((Registration) obj).getPrimaryTelephoneNumber(), res);
        res = checkString(this.getPrimaryEmailAddress(), ((Registration) obj).getPrimaryEmailAddress(), res);
        res = checkString(this.getTotalFee(), ((Registration) obj).getTotalFee(), res);
        res = checkString(this.getRegistrationFee(), ((Registration) obj).getRegistrationFee(), res);
        res = checkString(this.getCopyCardFee(), ((Registration) obj).getCopyCardFee(), res);
        res = checkString(this.getCopyCards(), ((Registration) obj).getCopyCards(), res);
        res = checkString(this.getAccountEmail(), ((Registration) obj).getAccountEmail(), res);
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
