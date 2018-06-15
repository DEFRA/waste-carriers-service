package uk.gov.ea.wastecarrier.services.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.PostBodyTask;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.EntityCsvReader;
import uk.gov.ea.wastecarrier.services.core.Entity;
import uk.gov.ea.wastecarrier.services.dao.EntityDao;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

// POST http://localhost:8005/tasks/entity-populator
public class EntityPopulatorTask extends PostBodyTask {

    private final EntityCsvReader reader;
    private final EntityDao dao;
    private final String filePath;

    private static Logger log = Logger.getLogger(EntityPopulatorTask.class.getName());

    public EntityPopulatorTask(
            String name,
            DatabaseConfiguration databaseConfig,
            String entityMatchingSeedFile
    ) {
        super(name);

        this.reader = new EntityCsvReader();
        this.dao = new EntityDao(databaseConfig);
        this.filePath = entityMatchingSeedFile;
    }

    public void execute(ImmutableMultimap<String, String> arg0, String body, PrintWriter out) {

        try {
            dao.recreate(entities(body));
        } catch (Exception e) {
            log.severe("Caught exception: " + e.getMessage());
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    private List<Entity> entities(String body) throws java.io.IOException {
        List<Entity> entities = null;

        if (body == null || body.isEmpty()) {
            entities = reader.read(this.filePath);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            TypeFactory factory = mapper.getTypeFactory();
            entities = mapper.readValue(body, factory.constructCollectionType(List.class, Entity.class));
        }
        return entities;
    }
}
