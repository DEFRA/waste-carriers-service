package uk.gov.ea.wastecarrier.services.tasks;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.EntityCsvReader;
import uk.gov.ea.wastecarrier.services.core.Entity;
import uk.gov.ea.wastecarrier.services.dao.EntityDao;

import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

// POST http://localhost:8005/tasks/entity-populator
public class EntityPopulator extends Task {

    private final EntityCsvReader reader;
    private final EntityDao dao;
    private final String filePath;

    private static Logger log = Logger.getLogger(EntityPopulator.class.getName());

    public EntityPopulator(
            String name,
            DatabaseConfiguration databaseConfig,
            String entityMatchingSeedFile
    ) {
        super(name);

        this.reader = new EntityCsvReader();
        this.dao = new EntityDao(databaseConfig);
        this.filePath = entityMatchingSeedFile;
    }

    public void execute(ImmutableMultimap<String, String> arg0, PrintWriter out) {

        try {
            List<Entity> entities = reader.read(this.filePath);
            dao.recreate(entities);
        } catch (Exception e) {
            log.severe("Caught exception: " + e.getMessage());
        }
    }
}
