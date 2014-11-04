/**
 * 
 */
package uk.gov.ea.wastecarrier.services.tasks;

import java.io.PrintWriter;
import java.util.logging.Logger;

import uk.gov.ea.wastecarrier.services.mongoDb.RegistrationsMongoDao;

import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.tasks.Task;

/**
 * DropWizard task to ensure that indexes have been defined on the MongoDb database collections.
 * @author gmueller
 *
 */
public class EnsureDatabaseIndexesTask extends Task {

	/** Logger for this class. */
	private Logger log = Logger.getLogger(EnsureDatabaseIndexesTask.class.getName());
	
	/** The DAO (injected). */
	private RegistrationsMongoDao dao;
	
	/**
	 * Constructor.
	 * @param name the task name
	 * @param dao the DAO
	 */
	public EnsureDatabaseIndexesTask(String name, RegistrationsMongoDao dao) {
		super(name);
		this.dao = dao;
	}

	/**
	 * @see com.yammer.dropwizard.tasks.Task#execute(com.google.common.collect.ImmutableMultimap, java.io.PrintWriter)
	 */
	@Override
	public void execute(ImmutableMultimap<String, String> parameters,
			PrintWriter output) throws Exception {
		log.info("Executing the task - ensuring indexes have been defined in the database.");
		dao.ensureIndexes();
		log.info("Task completed.");
	}

}