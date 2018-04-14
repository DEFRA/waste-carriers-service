/**
 * 
 */
package uk.gov.ea.wastecarrier.services.elasticsearch;

import io.dropwizard.lifecycle.Managed;
import org.elasticsearch.client.Client;


/**
 * Managed class to close the singleton ElasticSearch TransportClient on
 * shutdown.
 * 
 * @author gmueller
 * 
 */
public class ElasticSearchManaged implements Managed {

	/** The client to be closed. */
	private Client esClient;

	/**
	 * 
	 */
	public ElasticSearchManaged(Client esClient) {
		this.esClient = esClient;
	}

	@Override
	public void start() throws Exception {
		// Do nothing
	}

	@Override
	public void stop() throws Exception {
		esClient.close();
	}

}
