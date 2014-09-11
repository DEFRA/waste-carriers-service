package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.Calendar;
import java.util.Date;

import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.MetaData.RouteType;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.core.Settings;
import uk.gov.ea.wastecarrier.services.core.MetaData.RegistrationStatus;
import uk.gov.ea.wastecarrier.services.core.Registration.RegistrationTier;
import uk.gov.ea.wastecarrier.services.core.User;

public class PaymentHelper
{
	private Settings settings;
	
	public PaymentHelper(Settings settings) {

        this.settings = settings;
    }
	
	public boolean isReadyToBeActivated(Registration registration, User user)
	{
		boolean balanceValid = true;
		if (registration.getTier().equals(RegistrationTier.UPPER))
		{
			balanceValid = registration.getFinanceDetails().getBalance() == 0;
		}
		if (balanceValid
				&& registration.getCriminallySuspect() == null
				&& registration.getMetaData().getStatus().equals(RegistrationStatus.PENDING))
		{
			// Activate assisted digital routes without checking user
			if (registration.getMetaData().getRoute().equals(RouteType.ASSISTED_DIGITAL))
			{
				return true;
			}
			// For digital routes check the user has confirmed there account
			else if (registration.getMetaData().getRoute().equals(RouteType.DIGITAL)
					&& user != null
					&& user.getConfirmed_at() != null)
			{
				return true;
			}
		}
		return false;
	}
	
	public Registration setupRegistrationForActivation(Registration registration)
	{
		// Make registration active
		MetaData md = registration.getMetaData();
		md.setLastModified(MetaData.getCurrentDateTime());
		
		// Update Activation status and time
		md = makeActive(md);
		
		registration.setMetaData(md);
		
		// Set expires on date
		registration.setExpiresOn(getExpiryDate());
		return registration;
	}
	
	/**
	 * Updates the MetaData to give it an active status
	 * @param md
	 * @return
	 */
	private MetaData makeActive(MetaData md)
	{
		// Update Activation status and time
		if (MetaData.RegistrationStatus.PENDING.equals(md.getStatus()))
		{
			md.setDateActivated(MetaData.getCurrentDateTime());
			md.setStatus(MetaData.RegistrationStatus.ACTIVE);
		}
		return md;
	}
	
	/**
	 * Create an updated expired date based on the current time and the 
	 * settings provided
	 * @return
	 */
	private Date getExpiryDate()
	{
		// Set expires on date
		Calendar cal = Calendar.getInstance();
		String[] regPeriodList = settings.getRegistrationPeriod().split(" ");
		int length = Integer.parseInt(regPeriodList[0]);
		String type = regPeriodList[1];			
		if (type.equalsIgnoreCase("YEARS"))
		{
			cal.add(Calendar.YEAR, length);
		}
		else if (type.equalsIgnoreCase("MONTHS"))
		{
			cal.add(Calendar.MONTH, length);
		}
		else if (type.equalsIgnoreCase("DAYS"))
		{
			cal.add(Calendar.DAY_OF_MONTH, length);
		}
		Date expiryDate = cal.getTime();
		return expiryDate;
	}
}
