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
    private MatchResult matchResult;

    @JsonProperty("matching_system")
    private String matchingSystem;

    @JsonProperty("reference")
    private String reference;
    
    @JsonProperty("matched_name")
    private String matchedName;

    @JsonProperty("searched_at")
    private Date searchedAt;

    @JsonProperty("confirmed")
    private String confirmed;

    @JsonProperty("confirmed_at")
    private Date confirmedAt;

    @JsonProperty("confirmed_by")
    private String confirmedBy;

    public ConvictionSearchResult() {
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

    public MatchResult getMatchResult() {
        return matchResult;
    }

    public void setMatchResult(MatchResult matched) {
        this.matchResult = matched;
    }

    public String getMatchingSystem() {
        return matchingSystem;
    }

    public void setMatchingSystem(String matchingSystem) {
        this.matchingSystem = matchingSystem;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public String getMatchedName() {
        return matchedName;
    }
    
    public void setMatchedName(String matchedName) {
        this.matchedName = matchedName;
    }

    public Date getSearchedAt() {
        return searchedAt;
    }

    public void setSearchedAt(Date searchedAt) {
        this.searchedAt = searchedAt;
    }

    public String getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(String confirmed) {
        this.confirmed = confirmed;
    }

    public Date getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Date confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(String confirmedBy) {
        this.confirmedBy = confirmedBy;
    }
}
