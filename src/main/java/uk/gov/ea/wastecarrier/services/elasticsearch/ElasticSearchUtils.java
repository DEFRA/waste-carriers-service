/**
 * 
 */
package uk.gov.ea.wastecarrier.services.elasticsearch;

import java.util.logging.Logger;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import uk.gov.ea.wastecarrier.services.ElasticSearchConfiguration;

/**
 * 
 * Helper methods for dealing with ElasticSearch.
 * @author gmueller
 *
 */
public class ElasticSearchUtils {

	// Standard logging declaration
	private static Logger log = Logger.getLogger(ElasticSearchUtils.class.getName());

	/**
	 * 
	 * @return settings initialized with ping timeouts and more.
	 */
	public static Settings buildSettings() {
		log.info("building ElasticSearch transport settings");
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("client.transport.ping_timeout","10s")
				.put("client.transport.nodes_sampler_interval","10s")
				.build();
		return settings;
	}
	
	/**
	 * 
	 * @param esConfig the ElasticSearchConfiguration
	 * @return a new TransportClient
	 */
	public static TransportClient getNewTransportClient(ElasticSearchConfiguration esConfig) {
		log.info("building new TransportClient");
		TransportClient transportClient = new TransportClient(buildSettings())
			.addTransportAddress(new InetSocketTransportAddress(esConfig.getHost(), esConfig.getPort()));
		return transportClient;
	}
}
