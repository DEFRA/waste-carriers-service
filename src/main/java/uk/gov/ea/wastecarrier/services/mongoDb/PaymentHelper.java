package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.Calendar;
import java.util.Date;

import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.MetaData.RouteType;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.core.Settings;
import uk.gov.ea.wastecarrier.services.core.MetaData.RegistrationStatus;
import uk.gov.ea.wastecarrier.services.core.User;

public class PaymentHelper
{
	private Settings settings;
	
	public PaymentHelper(Settings settings) {

        this.settings = settings;
    }
	
	public boolean isReadyToBeActivated(Registration registration, User user)
	{
		if (registration.getFinanceDetails().getBalance() == 0 
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
		if (!MetaData.RegistrationStatus.ACTIVE.equals(md.getStatus()))
		{
			md.setDateActivated(MetaData.getCurrentDateTime());
			md.setStatus(MetaData.RegistrationStatus.ACTIVE);
		}
		registration.setMetaData(md);
		
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
		registration.setExpiresOn(expiryDate);
		return registration;
	}
}
