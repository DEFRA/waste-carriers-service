package uk.gov.ea.wastecarrier.services.dao;

import org.mongojack.JacksonDBCollection;

public interface IDataAccessObject<T> {

    JacksonDBCollection<T, String> getCollection();
}
