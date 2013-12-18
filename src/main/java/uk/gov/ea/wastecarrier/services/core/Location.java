package uk.gov.ea.wastecarrier.services.core;

/**
 * Location represents the geo_point location object used for GEO location searching in elastic search.
 * It contains a individual latitude and longitude values to represent the XY coordinate location of the site
 * being registered.
 * 
 * @author Steve
 * 
 */
public class Location {
	
    private double lat;
	
    private double lon;
	
	/**
	 * This empty default constructor is needed for JSON to be happy. The alternative is add
	 * "@JsonProperty("id")" in-front of the "long id" definition in the fully qualified constructor
	 */
	public Location()
	{
	}
	
	public Location(double lat, double lon)
	{
		this.lat = lat;
		this.lon = lon;
	}

    public double getLon() {
        return lon;
    }
    
    public double getLat() {
        return lat;
    }

	
	/**
	 * @param lon the lon to set
	 */
	public void setLon(double lon)
	{
		this.lon = lon;
	}
	
	/**
	 * @param lat the lat to set
	 */
	public void setLat(double lat)
	{
		this.lat = lat;
	}

}
