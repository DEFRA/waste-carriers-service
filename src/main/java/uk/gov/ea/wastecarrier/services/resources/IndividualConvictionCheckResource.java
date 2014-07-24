/**
 * 
 */
package uk.gov.ea.wastecarrier.services.resources;

import java.util.Date;

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
 * Dropwizard resource to provide conviction checks against individuals.
 * @author gmueller
 *
 */
@Path("/convictions/individuals.json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IndividualConvictionCheckResource {

	/**
	 * 
	 */
	public IndividualConvictionCheckResource(ElasticSearchConfiguration elasticSearchConfiguration) {
		// TODO Auto-generated constructor stub
	}

	@GET
	@Timed
	public ConvictionCheckResult hasConvictions(
			@QueryParam("firstName") String firstName, 
			@QueryParam("lastName") String lastName,
			@QueryParam("dateOfBirth") Date dateOfBirth)
	{
		//The last name is required
		if (lastName == null || lastName.trim().isEmpty())
		{
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		//...and the first name is required as well
		if (firstName == null || firstName.trim().isEmpty())
		{
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		//TODO - ...and the date of birth should be required as well
		if (dateOfBirth == null)
		{
		//	throw new WebApplicationException(Status.BAD_REQUEST);
		}
		

		//TODO Stubbed for now - Implement proper service
		return new ConvictionCheckResult(firstName.indexOf("Nigel") > -1, null, null, firstName, lastName, dateOfBirth);
	}

}
