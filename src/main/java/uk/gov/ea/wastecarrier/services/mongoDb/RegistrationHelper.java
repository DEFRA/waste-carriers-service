package uk.gov.ea.wastecarrier.services.mongoDb;

import java.util.Date;

import uk.gov.ea.wastecarrier.services.core.ConvictionSearchResult;
import uk.gov.ea.wastecarrier.services.core.ConvictionSignOff;
import uk.gov.ea.wastecarrier.services.core.KeyPerson;
import uk.gov.ea.wastecarrier.services.core.MetaData;
import uk.gov.ea.wastecarrier.services.core.Registration;
import uk.gov.ea.wastecarrier.services.core.MetaData.RegistrationStatus;

public class RegistrationHelper {

    public static Boolean isAwaitingConvictionConfirmation(Registration registration) {

        if (registration.getConvictionSignOffs() != null) {
            for (ConvictionSignOff signOff : registration.getConvictionSignOffs()) {
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
            if (searchResult.getMatchResult() != ConvictionSearchResult.MatchResult.NO
                    && searchResult.getConfirmed().equalsIgnoreCase("no")) {
                return true;
            }
        }

        if (registration.getKeyPeople() != null) {
            for (KeyPerson person : registration.getKeyPeople()) {
                searchResult = person.getConvictionSearchResult();
                if (searchResult != null) {
                    if (searchResult.getMatchResult() != ConvictionSearchResult.MatchResult.NO
                            && searchResult.getConfirmed().equalsIgnoreCase("no")) {
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
    	if (registration.getTier().equals(Registration.RegistrationTier.UPPER))
    	{
			Date currentDate = new Date();
			Date expiresDate = registration.getExpires_on();
			return currentDate.getTime() > expiresDate.getTime();
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
