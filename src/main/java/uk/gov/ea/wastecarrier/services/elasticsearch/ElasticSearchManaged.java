/**
 * 
 */
package uk.gov.ea.wastecarrier.services.elasticsearch;

import org.elasticsearch.client.Client;

import com.yammer.dropwizard.lifecycle.Managed;

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

	/**
	 * @see com.yammer.dropwizard.lifecycle.Managed#start()
	 */
	@Override
	public void start() throws Exception {
		// Do nothing
	}

	/**
	 * @see com.yammer.dropwizard.lifecycle.Managed#stop()
	 */
	@Override
	public void stop() throws Exception {
		esClient.close();
	}

}
