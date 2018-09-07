package uk.gov.ea.wastecarrier.services.dao;

import org.mongojack.JacksonDBCollection;

public interface ICanGetCollection<T> {

    JacksonDBCollection<T, String> getCollection();
}
