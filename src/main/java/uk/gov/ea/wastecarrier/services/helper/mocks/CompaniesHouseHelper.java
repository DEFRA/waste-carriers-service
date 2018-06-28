package uk.gov.ea.wastecarrier.services.helper.mocks;

import uk.gov.ea.wastecarrier.services.helper.ResourceHelper;

public class CompaniesHouseHelper {

    private static final String CH_RESOURCE_PATH = "mocks/companies";
    private static final String EMPTY_RESPONSE = "{}";

    private final ResourceHelper resourceHelper;

    public CompaniesHouseHelper() {
        this.resourceHelper = new ResourceHelper();
    }

    public String response(String companyNumber) {

        // If no company number is provided Companies house just sends an empty
        // json response
        if (companyNumber == null || companyNumber.isEmpty()) return EMPTY_RESPONSE;

        String filename = companyNumber.trim().toUpperCase() + ".json";

        String result = this.resourceHelper.openResourceFile(CH_RESOURCE_PATH, filename);

        if (result == null || result.isEmpty()) return notFound();

        return result;
    }

    private String notFound() {
        return this.resourceHelper.openResourceFile(CH_RESOURCE_PATH, "not_found.json");
    }
}
