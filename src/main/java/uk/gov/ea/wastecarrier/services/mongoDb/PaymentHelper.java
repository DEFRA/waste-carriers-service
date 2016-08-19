package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.MetaData.RouteType;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.core.Settings;
import uk.gov.ea.wastecarrier.services.core.MetaData.RegistrationStatus;
import uk.gov.ea.wastecarrier.services.core.Registration.RegistrationTier;
import uk.gov.ea.wastecarrier.services.core.User;
import uk.gov.ea.wastecarrier.services.core.FinanceDetails;

public class PaymentHelper
{
    private Logger log = Logger.getLogger(PaymentHelper.class.getName());
    
    public static final String IR_REGISTRATION_NO_PREFIX = "CB/";

    private Settings settings;

    public PaymentHelper(Settings settings)
    {
        this.settings = settings;
    }

    public boolean isReadyToBeActivated(Registration registration, User user)
    {
        boolean result = false;

        MetaData metaData = registration.getMetaData();
        if (metaData == null)
        {
            log.warning("Registration is missing metaData field");
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
        
        RegistrationStatus status = metaData.getStatus();
        RegistrationTier tier = registration.getTier();
        RouteType route = metaData.getRoute();

        if ((status == null) || (tier == null) || (route == null))
        {
            log.warning("Registration is missing status OR tier OR route field");
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        // Can only be activated if currently pending or refused.
        if (status.equals(RegistrationStatus.PENDING) || status.equals(RegistrationStatus.REFUSED))
        {
            // Only activate if no money is owed, and no convictions checks are pending.
            if (isBalanceValid(registration) && !RegistrationHelper.isAwaitingConvictionConfirmation(registration))
            {
                // Assisted Digital registrations are activated immediately, as
                // are Upper Tier registrations.
                if (route.equals(RouteType.ASSISTED_DIGITAL) || tier.equals(RegistrationTier.UPPER))
                {
                    result = true;
                }

                // Lower Tier non-AD registrations can only become active once
                // the account holder has validated their account.
                else if (tier.equals(RegistrationTier.LOWER))
                {
                    result = isUserValid(user);
                }
            }
        }
        
        return result;
    }

    public boolean isReadyToBeRenewed(Registration registration)
    {
        if (registration.getRenewalRequested() != null && registration.getRenewalRequested().equalsIgnoreCase("true"))
        {
            // Renewal requested
            return true;
        }

        return false;
    }

    public Boolean isUserValid(User user)
    {
        Boolean result = false;

        if (user != null && user.getConfirmed_at() != null)
        {
            result = true;
        }

        return result;
    }

    public Boolean isBalanceValid(Registration registration)
    {
        Boolean result;

        if (registration.getTier().equals(RegistrationTier.LOWER))
        {
            result = true;
        }
        else
        {
            FinanceDetails fd = registration.getFinanceDetails();
            result = ((fd != null) && (fd.getOrders() != null) && (fd.getOrders().size() > 0) && (fd.getBalance() <= 0));
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

        if (registration.getTier().equals(RegistrationTier.UPPER))
        {
            // Set expires on date
            registration.setExpires_on(getExpiryDate(registration));
        }
        return registration;
    }

    /**
     * Updates the MetaData to give it an active status
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
     * Create an updated expired date depending on registration type and the
     * settings provided
     */
    private Date getExpiryDate(Registration registration)
    {
        String[] regPeriodList = settings.getRegistrationPeriod().split(" ");
        int length = Integer.parseInt(regPeriodList[0]);
        Date expiryDate = null;

        // Detect standard or IR renewal
        if (isIRRenewal(registration) && irRenewalWasMadeBeforeDeadline(registration))
        {
            // Set expiry date to X years from current expiry date
            Calendar newExpiryDate = new GregorianCalendar();
            
            newExpiryDate.setTime(registration.getOriginalDateExpiry());
            newExpiryDate.add(Calendar.YEAR, length);

            expiryDate = newExpiryDate.getTime();
        }
        else
        {
            // Set expiry date to X years from now
            Calendar cal = Calendar.getInstance();
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

            expiryDate = cal.getTime();
        }

        return expiryDate;
    }

    private static boolean isIRRenewal(Registration registration)
    {
        Boolean result = false;

        if (registration.getOriginalRegistrationNumber() != null)
        {
            String regNo = registration.getOriginalRegistrationNumber().trim();

            result = (regNo != null && isIRRegistrationType(regNo));
        }

        return result;
    }
    
    private static boolean irRenewalWasMadeBeforeDeadline(Registration registration)
    {
        Date previousRegExpiryDate = registration.getOriginalDateExpiry();
        Date applicationDate = registration.getMetaData().getDateRegistered();
        return applicationDate.before(previousRegExpiryDate);
    }

    private static boolean isIRRegistrationType(String regNo)
    {
        return regNo.startsWith(IR_REGISTRATION_NO_PREFIX);
    }
}
