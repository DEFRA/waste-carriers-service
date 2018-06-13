package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConvictionSearchResult {

    public enum MatchResult {
        YES, NO, NA, UNKNOWN
    }

    @JsonProperty("match_result")
    public MatchResult matchResult;

    @JsonProperty("matching_system")
    public String matchingSystem;

    @JsonProperty("reference")
    public String reference;
    
    @JsonProperty("matched_name")
    public String matchedName;

    @JsonProperty("searched_at")
    public Date searchedAt;

    @JsonProperty("confirmed")
    public String confirmed;

    @JsonProperty("confirmed_at")
    public Date confirmedAt;

    @JsonProperty("confirmed_by")
    public String confirmedBy;

    public ConvictionSearchResult() {
        this.matchResult = MatchResult.NO;
        this.confirmed = "no";
    }

    public ConvictionSearchResult(
            MatchResult matched,
            String matchingSystem,
            String reference,
            String matchedName,
            Date searchedAt,
            String confirmed,
            Date confirmedAt,
            String confirmedBy) {

        this.matchResult = matched;
        this.matchingSystem = matchingSystem;
        this.reference = reference;
        this.matchedName = matchedName;
        this.searchedAt = searchedAt;
        this.confirmed = confirmed;
        this.confirmedAt = confirmedAt;
        this.confirmedBy = confirmedBy;
    }

    public void update(Entity matchedEntity) {
        if (matchedEntity == null) return;

        this.matchResult = MatchResult.YES;
        this.matchingSystem = matchedEntity.systemFlag;
        this.reference = matchedEntity.incidentNumber;
        this.matchedName = matchedEntity.name;
    }
}
