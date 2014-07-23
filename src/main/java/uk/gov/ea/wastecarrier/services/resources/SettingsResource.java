package uk.gov.ea.wastecarrier.services.resources;

import uk.gov.ea.wastecarrier.services.SettingsConfiguration;
import uk.gov.ea.wastecarrier.services.core.Settings;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.logging.Logger;

/**
 * This class represents the order details URL (defined at @Path) and associated operations for POSTing
 * orders
 * 
 */
@Path("/"+Settings.COLLECTION_NAME+".json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SettingsResource
{	
	private Logger log = Logger.getLogger(SettingsResource.class.getName());
	
	private Settings settings;
	
	/**
	 * 
	 * @param settings SettingsConfiguration parameters for the Settigns service
	 */
	public SettingsResource(SettingsConfiguration settings)
	{
		this.settings = new Settings(settings);
	}

	/**
	 * GETS the list of reusable generic parameters that are business configurable by the waste carrier forms.
	 */
	@GET
	public Settings getSettings()
	{
		log.info("GET METHOD detected in getSettings() method for getting settings");
		return settings;
	}

}
