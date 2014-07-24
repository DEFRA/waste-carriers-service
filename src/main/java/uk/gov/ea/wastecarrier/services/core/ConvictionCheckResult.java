/**
 * 
 */
package uk.gov.ea.wastecarrier.services.core;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the result of a conviction check.
 * Initially we only report whether there was a 'match', indicating 
 * that there was a matching record found in the conviction data available.
 * Other information (such as a match score,list of matching persons etc.) may be added later.
 * 
 * @author gmueller
 *
 */
public class ConvictionCheckResult {

	/**
	 * True if there is a match, false otherwise.
	 */
	@JsonProperty
	private boolean match;
	
	@JsonProperty
	private String companyName;
	
	@JsonProperty
	private String companyNumber;
	
	@JsonProperty
	private String firstName;
	
	@JsonProperty
	private String lastName;
	
	@JsonProperty
	private Date dateOfBirth;
	
	
	/**
	 * Constructor(s) with arguments
	 */
	public ConvictionCheckResult(boolean match, String companyName, String companyNumber, String firstName, String lastName, Date dateOfBirth) {
		this.match = match;
		this.companyName = companyName;
		this.companyNumber = companyNumber;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dateOfBirth = dateOfBirth;
	}

	/**
	 * Constructor(s) with arguments
	 */
	public ConvictionCheckResult(boolean match) {
		this.match = match;
	}

}
