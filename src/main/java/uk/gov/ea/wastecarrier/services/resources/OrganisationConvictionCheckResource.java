/**
 * 
 */
package uk.gov.ea.wastecarrier.services.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.yammer.metrics.annotation.Timed;

import uk.gov.ea.wastecarrier.services.ElasticSearchConfiguration;
import uk.gov.ea.wastecarrier.services.core.ConvictionCheckResult;


/**
 * 
 * Dropwizard resource to provide conviction checks against organisations.
 * @author gmueller
 *
 */
@Path("/convictions/organisations.json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrganisationConvictionCheckResource {

	/**
	 * 
	 */
	public OrganisationConvictionCheckResource(ElasticSearchConfiguration elasticSearchConfiguration) 
	{
		//TODO Any dependencies (config? dao?) to follow
	}

	/**
	 * 
	 * @param companyName
	 * @param companyNumber
	 * @return true if there is a matching record in our conviction data, false otherwise
	 */
	@GET
	@Timed
	public ConvictionCheckResult hasConvictions(
			@QueryParam("companyName") String companyName, 
			@QueryParam("companyNumber") String companyNumber)
	{
		//The companyName is required
		if (companyName == null || companyName.trim().isEmpty())
		{
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		//TODO Stubbed for now - Implement proper service
		return new ConvictionCheckResult(companyName.indexOf("Nigel") > -1, companyName, companyNumber, null, null, null);
	}
}
