package uk.gov.ea.wastecarrier.services.mongoDb;

import uk.gov.ea.wastecarrier.services.core.ConvictionSearchResult;
import uk.gov.ea.wastecarrier.services.core.ConvictionSignOff;
import uk.gov.ea.wastecarrier.services.core.KeyPerson;
import uk.gov.ea.wastecarrier.services.core.Registration;

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
}
