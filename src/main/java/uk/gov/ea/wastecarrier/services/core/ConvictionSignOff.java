package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class ConvictionSignOff {

    @JsonProperty("confirmed")
    private String confirmed;

    @JsonProperty("confirmed_at")
    private Date confirmedAt;

    @JsonProperty("confirmed_by")
    private String confirmedBy;

    public ConvictionSignOff() {
    }

    public ConvictionSignOff(String confirmed, Date confirmedAt, String confirmedBy) {
        this.confirmed = confirmed;
        this.confirmedAt = confirmedAt;
        this.confirmedBy = confirmedBy;
    }

    public String getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(String confirmedBy) {
        this.confirmedBy = confirmedBy;
    }

    public Date getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Date confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(String confirmed) {
        this.confirmed = confirmed;
    }
}
