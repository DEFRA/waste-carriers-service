package uk.gov.ea.wastecarrier.services.health;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;

import uk.gov.ea.wastecarrier.services.core.Registration;

import com.yammer.metrics.core.HealthCheck;

public class ElasticSearchHealthCheck extends HealthCheck {
    private Client esClient;

    public ElasticSearchHealthCheck(Client elasticSearchClient) {
        super("ElasticSearch");
        esClient = elasticSearchClient;
    }

    @Override
    protected Result check() throws Exception {
    	try
    	{
	    	GetResponse response = esClient.prepareGet(Registration.COLLECTION_NAME, "registration", "1")
	    	        .execute()
	    	        .actionGet();
	    	response.getVersion();
    	}
		catch (NoNodeAvailableException e)
		{
			return Result.unhealthy("Cannot get basic response from Elastic Search, Check service is running",e);
		}
        return Result.healthy();
    }
}
