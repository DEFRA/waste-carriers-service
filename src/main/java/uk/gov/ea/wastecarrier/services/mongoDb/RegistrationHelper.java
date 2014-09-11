package uk.gov.ea.wastecarrier.services.mongoDb;

import uk.gov.ea.wastecarrier.services.core.ConvictionSearchResult;
import uk.gov.ea.wastecarrier.services.core.KeyPerson;
import uk.gov.ea.wastecarrier.services.core.Registration;

public class RegistrationHelper {

    public static Boolean isAwaitingConvictionConfirmation(Registration registration) {

        ConvictionSearchResult searchResult = registration.getConvictionSearchResult();
        if (searchResult != null) {
            if (searchResult.getMatchResult() != ConvictionSearchResult.MatchResult.NO
                    && registration.getConvictionSearchResult().getConfirmed().equalsIgnoreCase("no")) {
                return true;
            }
        }

        for (KeyPerson person : registration.getKeyPeople()) {
            searchResult = person.getConvictionSearchResult();
            if (searchResult != null) {
                if (searchResult.getMatchResult() != ConvictionSearchResult.MatchResult.NO
                        && searchResult.getConfirmed().equalsIgnoreCase("no")) {
                    return true;
                }
            }
        }

        return false;
    }
}
