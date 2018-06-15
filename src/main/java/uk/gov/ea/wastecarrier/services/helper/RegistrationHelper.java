package uk.gov.ea.wastecarrier.services.helper;

import java.util.Date;
import java.util.logging.Logger;

import uk.gov.ea.wastecarrier.services.core.ConvictionSearchResult;
import uk.gov.ea.wastecarrier.services.core.ConvictionSignOff;
import uk.gov.ea.wastecarrier.services.core.KeyPerson;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.core.MetaData.RegistrationStatus;

public class RegistrationHelper {

    /** logger for this class. */
    private static Logger log = Logger.getLogger(RegistrationHelper.class.getName());

    public static Boolean isAwaitingConvictionConfirmation(Registration registration) {

        if (registration.getConviction_sign_offs() != null) {
            for (ConvictionSignOff signOff : registration.getConviction_sign_offs()) {
                if (signOff.getConfirmed().equalsIgnoreCase("no")) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Boolean hasUnconfirmedConvictionMatches(Registration registration) {

        ConvictionSearchResult searchResult = registration.getConvictionSearchResult();
        if (searchResult != null) {
            if (searchResult.matchResult != ConvictionSearchResult.MatchResult.NO
                    && searchResult.confirmed.equalsIgnoreCase("no")) {
                return true;
            }
        }

        if (registration.getKeyPeople() != null) {
            for (KeyPerson person : registration.getKeyPeople()) {
                searchResult = person.getConvictionSearchResult();
                if (searchResult != null) {
                    if (searchResult.matchResult != ConvictionSearchResult.MatchResult.NO
                            && searchResult.confirmed.equalsIgnoreCase("no")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    
    /**
     * Check if today is beyond the expired date
     */
    public static Boolean hasExpired(Registration registration)
    {
        if (registration.getTier() != null && registration.getTier().equals(Registration.RegistrationTier.UPPER))
        {
            Date currentDate = new Date();
            Date expiresDate = registration.getExpires_on();
            if (expiresDate != null)
            {
                return currentDate.getTime() > expiresDate.getTime();
            }
            else
            {
                log.warning("Could not find an expiry date for Upper tier registration: " + registration.getId());
                log.info("Marking registration as expired as the expiry date cannot be found");
                return true;
            }
        }
        return false;
    }
    
    /**
     * Sets registration as EXPIRED
     */
    public static Registration setAsExpired(Registration registration)
    {
        MetaData md = registration.getMetaData();
        md.setStatus(RegistrationStatus.EXPIRED);
        md.setLastModified(MetaData.getCurrentDateTime());
        registration.setMetaData(md);
        return registration;
    }
}
