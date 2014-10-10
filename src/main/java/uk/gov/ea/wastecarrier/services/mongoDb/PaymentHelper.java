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
        if (registration.getMetaData().getStatus().equals(RegistrationStatus.PENDING) 
        		|| registration.getMetaData().getStatus().equals(RegistrationStatus.REFUSED)) {

            // Activate assisted digital routes without checking user
            if (registration.getMetaData().getRoute().equals(RouteType.ASSISTED_DIGITAL)
            		&& isBalanceValid(registration)
                    && !RegistrationHelper.isAwaitingConvictionConfirmation(registration))
            {
                return true;
            }

            if (registration.getTier().equals(RegistrationTier.LOWER)
                    && registration.getMetaData().getRoute().equals(RouteType.DIGITAL)
                    && isUserValid(user)) {
                return true;
            }
            else if (isBalanceValid(registration)
                    && !RegistrationHelper.isAwaitingConvictionConfirmation(registration)) {
                return true;
            }
        }

		return false;
	}
	
	public boolean isReadyToBeRenewed(Registration registration)
	{
        if (registration.getRenewalRequested() != null && registration.getRenewalRequested().equalsIgnoreCase("true")) {
        	// Renewal requested
        	return true;
        }

		return false;
	}

    public Boolean isUserValid(User user) {

        Boolean result = false;

        if (user != null && user.getConfirmed_at() != null) {
            result = true;
        }

        return result;
    }

    public Boolean isBalanceValid(Registration registration) {

        Boolean result;

        if (registration.getTier().equals(RegistrationTier.LOWER)) {
            result = true;
        } else {
            result = registration.getFinanceDetails().getBalance() == 0;
        }

        return result;
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
		registration.setExpires_on(getExpiryDate());
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
		if (MetaData.RegistrationStatus.PENDING.equals(md.getStatus()) 
				|| MetaData.RegistrationStatus.REFUSED.equals(md.getStatus()))
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
