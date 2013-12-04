package uk.gov.ea.wastecarrier.services.resources;

import java.io.IOException;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import uk.gov.ea.wastecarrier.services.core.Version;

import com.yammer.metrics.annotation.Timed;

/**
 * This class represents a GET and PUT Resource provided as the @Path parameter. Specifically serving the Edit and
 * Update services
 * 
 */
@Path("/" + Version.VERSION_SINGULAR_NAME + ".json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistrationVersionResource
{
	
	// Standard logging declaration
	private Logger log = Logger.getLogger(RegistrationVersionResource.class.getName());

	/**
	 * 
	 */
	public RegistrationVersionResource()
	{
	}

	/**
	 * This retrieves the version number of this registration API service
	 * 
	 * @return if found returns the version string from the running jar, otherwise returns a default version
	 */
	@GET
	@Timed
	public Version getVersion()
	{
		log.info("Get Method Detected, attempt to return version details");

		Version v = new Version();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		// Get and Print the Jar Version to the console for logging purposes
        Package objPackage = this.getClass().getPackage();
        if (objPackage.getImplementationVersion() != null)
        {
        	// Only get version if running as a Jar, otherwise these functions will not work
	        String version = objPackage.getImplementationVersion();
	        v.setVersionDetails(version);
	        
	        // Get approximate built time by assuming default resource time is jar built time
	        URLConnection ul;
			try
			{
				ul = this.getClass().getResource("").openConnection();
				v.setLastBuilt(formatter.format(new Date(ul.getLastModified())) );
			}
			catch (IOException e)
			{
				 log.info("Could not get last modified time out of jar, Using default manual value");
			}
        }
        else
        {
        	v.setVersionDetails("SNAPSHOT-Running from Eclipse");
        	v.setLastBuilt(formatter.format(new Date()) );
        }
		
		return v;
	}

}
