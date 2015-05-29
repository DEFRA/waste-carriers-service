package uk.gov.ea.wastecarrier.services.core;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * This class represents the entire model of the initial Waste Carrier Registration details
 * Ideally the models details could be split into subclasses for each distinct type of details (page)
 *
 */

/**
 * @author alancruikshanks
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CopyCards
{
	public final static String COLLECTION_SINGULAR_NAME = "copy_card";
	public final static String COLLECTION_NAME = COLLECTION_SINGULAR_NAME +"s";
}
