package uk.gov.ea.wastecarrier.services.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Location represents the geo_point location object used for GEO location searching in elastic search.
 * It contains a individual latitude and longitude values to represent the XY coordinate location of the site
 * being registered.
 * 
 * @author Steve
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {

    @JsonProperty()
    public double lat;

    @JsonProperty()
    public double lon;

    /**
     * This empty default constructor is needed for JSON to be happy. The alternative is add
     * "@JsonProperty("id")" in-front of the "long id" definition in the fully qualified constructor
     */
    public Location() { }

    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }
}
