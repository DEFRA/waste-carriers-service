package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple POJO for containing user details
 * @author Steve
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User
{
	/**
	 * 
	 * field :email,              :type => String, :default => ""
  field :encrypted_password, :type => String, :default => ""

  ## Recoverable
  field :reset_password_token,   :type => String
  field :reset_password_sent_at, :type => Time

  ## Rememberable
  field :remember_created_at, :type => Time

  ## Trackable
  field :sign_in_count,      :type => Integer, :default => 0
  field :current_sign_in_at, :type => Time
  field :last_sign_in_at,    :type => Time
  field :current_sign_in_ip, :type => String
  field :last_sign_in_ip,    :type => String

  ## Confirmable
  field :confirmation_token,   :type => String
  field :confirmed_at,         :type => Time
  field :confirmation_sent_at, :type => Time
  field :unconfirmed_email,    :type => String # Only if using reconfirmable

  ## Lockable
  field :failed_attempts, :type => Integer, :default => 0 # Only if lock strategy is :failed_attempts
  field :unlock_token,    :type => String # Only if unlock strategy is :email or :both
  field :locked_at,       :type => Time
	 */
	
	@JsonProperty
	private String email;

	@JsonProperty
	private String confirmed_at;
	
	@JsonProperty
	private String confirmation_sent_at;
	
	public final static String COLLECTION_SINGULAR_NAME = "user";
	public final static String COLLECTION_NAME = COLLECTION_SINGULAR_NAME +"s";
	
	public User()
	{
	}
	
	public User(String email, String confirmed_at,
			String confirmation_sent_at)
	{
		super();
		this.email = email;
		this.confirmed_at = confirmed_at;
		this.confirmation_sent_at = confirmation_sent_at;
	}
	
	/**
	 * @return the email
	 */
	public String getEmail()
	{
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email)
	{
		this.email = email;
	}

	/**
	 * @return the confirmed_at
	 */
	public String getConfirmed_at()
	{
		return confirmed_at;
	}

	/**
	 * @param confirmed_at the confirmed_at to set
	 */
	public void setConfirmed_at(String confirmed_at)
	{
		this.confirmed_at = confirmed_at;
	}

	/**
	 * @return the confirmation_sent_at
	 */
	public String getConfirmation_sent_at()
	{
		return confirmation_sent_at;
	}

	/**
	 * @param confirmation_sent_at the confirmation_sent_at to set
	 */
	public void setConfirmation_sent_at(String confirmation_sent_at)
	{
		this.confirmation_sent_at = confirmation_sent_at;
	}

}
