package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

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
	 * The tier - 'UPPER' or 'LOWER'
	 */
	public enum RegistrationTier {
		LOWER(Registration.REGID_PREFIX_LOWER),
        UPPER(Registration.REGID_PREFIX_UPPER);

        private String prefix;

        RegistrationTier(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
	}
	
	/**
	 * This is the Key for the Registration details
	 */
	@JsonProperty 
	@Id
	@ObjectId
    private String id;

	/*
	 * A unique identifier assigned by the client in order to make creation of registrations
	 * i.e. inserts (and the corresponding HTTP POST request)
	 * idempotent, and thus not suffer from accidental message re-sends.
	 * Note: We are not using the @NotEmpty annotation to validate - only validating on insert
	 * and we want to avoid issues with pre-existing data which did not set this during insert.
	 * 
	 * Because the frontend currently uses the term 'uuid' for other purposes (actually, for the id generated by MongoDB), 
	 * we report this property under a different name.
	 */
	@JsonProperty("reg_uuid")
	//@NotEmpty
	private String uuid;

	/**
	 * The registration tier
	 */
	@Valid
	@JsonProperty
	private RegistrationTier tier;
	
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
	@JsonProperty("company_no")
	private String companyNo;

	
	@JsonProperty
	private String uprn;

	/* 
	 * These are the Contact Details
	 */
	//Note: the title was mandatory in phase 1, bit not anymore
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

	@JsonProperty("addresses")
	private List<Address> addresses;

    @JsonProperty("key_people")
    private List<KeyPerson> keyPeople;
	
	/*
	 * Payment values
	 */
	@JsonProperty("total_fee")
	private String totalFee;
	@JsonProperty("registration_fee")
	private String registrationFee;
	@JsonProperty("copy_card_fee")
	private String copyCardFee;
	@JsonProperty("copy_cards")
	private String copyCards;
	
	@JsonProperty
	private String accountEmail;

    @JsonProperty
    private String declaredConvictions;
	
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
	
	
	/*
	 * The expiry date (only applicable for upper tier registrations)
	 */
	@JsonProperty("expires_on")
	@Valid
	private Date expires_on;

    @JsonProperty
	private String originalRegistrationNumber;

    @JsonProperty
    private Date originalDateExpiry;

    @JsonProperty
	private String renewalRequested;

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
	
	@JsonInclude(Include.NON_DEFAULT)
	private FinanceDetails financeDetails;

    @JsonProperty("conviction_search_result")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private ConvictionSearchResult convictionSearchResult;

    @JsonProperty("conviction_sign_offs")
    private List<ConvictionSignOff> conviction_sign_offs;
	
	// Standard logging declaration
	private Logger log = Logger.getLogger(Registration.class.getName());
	
	/**
	 * Default constructor is required for non complete objects, and as alternative to 
	 * defining specific JSON names per property
	 */
	public Registration ()
	{
	}
	
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
	 * @return the uuid
	 */
	public String getUuid()
	{
		return uuid;
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
     * @return the declaredConvictions
     */
    public String getDeclaredConvictions() {
        return declaredConvictions;
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
	 * @return the tier
	 */
	public RegistrationTier getTier() {
		return tier;
	}

	/**
	 * @param tier the tier to set
	 */
	public void setTier(RegistrationTier tier) {
		this.tier = tier;
	}

	/**
	 * @param uuid the id to set
	 */
	public void setUuid(String uuid)
	{
		this.uuid = uuid;
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
	 * @param contactEmail the email to set
	 */
	public void setContactEmail(String contactEmail)
	{
		this.contactEmail = contactEmail;
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
     * @param declaredConvictions
     */
    public void setDeclaredConvictions(String declaredConvictions) {
        this.declaredConvictions = declaredConvictions;
    }

	/**
	 * @param declaration the confirmDeclaration to set
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
	 * @return the expiresOn
	 */
	public Date getExpires_on() {
		return expires_on;
	}

	/**
	 * @param expiresOn the expiresOn to set
	 */
	public void setExpires_on(Date expiresOn) {
		this.expires_on = expiresOn;
	}

	/**
	 * @param metaData the metaData to set
	 */
	public void setMetaData(MetaData metaData)
	{
		this.metaData = metaData;
	}
	


	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location)
	{
		this.location = location;
	}

    public List<KeyPerson> getKeyPeople() {
        return keyPeople;
    }

    public void setKeyPeople(List<KeyPerson> keyPeople) {
        this.keyPeople = keyPeople;
    }

	public List<Address> getAddresses() { return addresses; }

	public void setAddresses(List<Address> addresses) { this.addresses = addresses; }

    public ConvictionSearchResult getConvictionSearchResult() {
        return convictionSearchResult;
    }

    public void setConvictionSearchResult(ConvictionSearchResult convictionSearchResult) {
        this.convictionSearchResult = convictionSearchResult;
    }

    public List<ConvictionSignOff> getConviction_sign_offs() {
        return conviction_sign_offs;
    }

    public void setConviction_sign_offs(List<ConvictionSignOff> convictionSignOffs) {
        this.conviction_sign_offs = convictionSignOffs;
    }

    public FinanceDetails getFinanceDetails()
    {
        return financeDetails;
    }

    public void setFinanceDetails(FinanceDetails financeDetails)
    {
        this.financeDetails = financeDetails;
    }

    public String getOriginalRegistrationNumber()
    {
        return originalRegistrationNumber;
    }

    public void setOriginalRegistrationNumber(String originalRegistrationNumber)
    {
        this.originalRegistrationNumber = originalRegistrationNumber;
    }
    
    /**
	 * @return the renewalRequested
	 */
	public String getRenewalRequested()
	{
		return renewalRequested;
	}


	/**
	 * @param renewalRequested the renewalRequested to set
	 */
	public void setRenewalRequested(String renewalRequested)
	{
		this.renewalRequested = renewalRequested;
	}

    public Date getOriginalDateExpiry() {
        return originalDateExpiry;
    }

    public void setOriginalDateExpiry(Date originalDateExpiry) {
        this.originalDateExpiry = originalDateExpiry;
    }

    public boolean validateUuid()
	{
		//TODO May want to use Apache Commons StringUtils or the like	
		return this.uuid != null && !this.uuid.isEmpty();
	}
	
    /**
	 * Custom comparison method for comparing the contents of the user entered fields 
	 * to ensure objects are the same.
	 * 
	 * Effectively created to enable automated testing of comparison objects.
	 */
	@Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Registration other = (Registration) obj;
        return Objects.equals(this.tier, other.tier)
                && Objects.equals(this.registrationType, other.registrationType)
                && Objects.equals(this.businessType, other.businessType)
                && Objects.equals(this.otherBusinesses, other.otherBusinesses)
                && Objects.equals(this.isMainService, other.isMainService)
                && Objects.equals(this.constructionWaste, other.constructionWaste)
                && Objects.equals(this.onlyAMF, other.onlyAMF)
                && Objects.equals(this.companyName, other.companyName)
                && Objects.equals(this.individualsType, other.individualsType)
                && Objects.equals(this.publicBodyType, other.publicBodyType)
                && Objects.equals(this.publicBodyTypeOther, other.publicBodyTypeOther)
                && Objects.equals(this.companyNo, other.companyNo)
                && Objects.equals(this.uprn, other.uprn)
                && Objects.equals(this.title, other.title)
                && Objects.equals(this.otherTitle, other.otherTitle)
                && Objects.equals(this.firstName, other.firstName)
                && Objects.equals(this.lastName, other.lastName)
                && Objects.equals(this.position, other.position)
                && Objects.equals(this.phoneNumber, other.phoneNumber)
                && Objects.equals(this.contactEmail, other.contactEmail)
                && Objects.equals(this.totalFee, other.totalFee)
                && Objects.equals(this.registrationFee, other.registrationFee)
                && Objects.equals(this.copyCardFee, other.copyCardFee)
                && Objects.equals(this.copyCards, other.copyCards)
                && Objects.equals(this.accountEmail, other.accountEmail)
                && Objects.equals(this.declaredConvictions, other.declaredConvictions)
                && Objects.equals(this.declaration, other.declaration)
                && Objects.equals(this.expires_on, other.expires_on)
                && Objects.equals(this.originalRegistrationNumber, other.originalRegistrationNumber)
                && Objects.equals(this.renewalRequested, other.renewalRequested);
    }

	@Override
	public int hashCode()
	{
        return Objects.hash(tier, registrationType, businessType, otherBusinesses, isMainService, constructionWaste,
                onlyAMF, companyName, individualsType, publicBodyType, publicBodyTypeOther, companyNo, uprn,
                title, otherTitle, firstName, lastName, position, phoneNumber, contactEmail, totalFee, registrationFee,
                copyCardFee, copyCards, accountEmail, declaredConvictions, declaration, expires_on, originalRegistrationNumber
            );
	}
}
