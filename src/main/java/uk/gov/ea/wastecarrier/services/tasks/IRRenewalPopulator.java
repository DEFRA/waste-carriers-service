package uk.gov.ea.wastecarrier.services.tasks;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.tasks.Task;

import uk.gov.ea.wastecarrier.services.DatabaseConfiguration;
import uk.gov.ea.wastecarrier.services.IRConfiguration;
import uk.gov.ea.wastecarrier.services.core.irdata.CompanyIRData;
import uk.gov.ea.wastecarrier.services.core.irdata.IRData;
import uk.gov.ea.wastecarrier.services.core.irdata.IndividualIRData;
import uk.gov.ea.wastecarrier.services.core.irdata.PartnersIRData;
import uk.gov.ea.wastecarrier.services.core.irdata.PublicBodyIRData;
import uk.gov.ea.wastecarrier.services.mongoDb.IRRenewalMongoDao;

/**
 * The IR Renewal Populator creates records in the mongoDB database from exported data from the legacy IR system.
 * That data can subsequently be queried and used to populate some registration data.
 * 
 * To use this service call, E.g. 
 * curl -X POST http://localhost:9091/tasks/ir-renewal 
 * Which performs a full re-population for entries in the csv's provided
 * 
 * @author Steve
 * 
 */
public class IRRenewalPopulator extends Task
{

	private ArrayList<IRData> irDataList;
	private IRRenewalMongoDao dao;
	private String irCompanyDataFilePath;
	private String irIndividualDataFilePath;
	private String irPartnersDataFilePath;
	private String irPublicBodyDataFilePath;
	private static Logger log = Logger.getLogger(IRRenewalPopulator.class.getName());
	
	public enum IRRenewal_Type {
	    Company,
	    Individual,
	    Partner,
	    PublicBody
	}
	
	
	/**
	 * Setup the Postcode Register
	 * 
	 * @param dao IRRenewalMongoDao used to perform database operations
	 * @param irCompanyData a string of the path to the Company Data CSV file
	 * @param irIndividualData a string of the path to the Individual Data CSV file
	 * @param irPartnersData a string of the path to the Partners Data CSV file
	 * @param irPublicBodyData a string of the path to the Public Body Data CSV file
	 */
	public IRRenewalPopulator(String name, DatabaseConfiguration database, IRConfiguration irConfig)
	{
		super(name);
		this.dao = new IRRenewalMongoDao(database);
		this.irCompanyDataFilePath = irConfig.getIrRenewalFolderPath() + irConfig.getIrRenewalCompanyFileName();
		this.irIndividualDataFilePath = irConfig.getIrRenewalFolderPath() + irConfig.getIrRenewalIndividualFileName();
		this.irPartnersDataFilePath = irConfig.getIrRenewalFolderPath() + irConfig.getIrRenewalPartnersFileName();
		this.irPublicBodyDataFilePath = irConfig.getIrRenewalFolderPath() + irConfig.getIrRenewalPublicBodyFileName();
		this.irDataList = new ArrayList<IRData>();
		log.fine("IR data populator loaded");
	}
	
	/**
	 * Performs the operation Used Via the administration ports to recreate the full IR renewal data
	 * in the mongo database. It first drops all existing data, then parses the 4 different CSV files
	 * and puts each row found in the database, (excluding header rows)
	 * 
	 * Usage: 
	 * curl -X POST http://[SERVER]:[ADMINPORT]/tasks/[this.getName()] 
	 */
	@Override
	public void execute(ImmutableMultimap<String, String> arg0, PrintWriter out) throws Exception
	{
		// Drop any existing IR renewal data
		dropAllIRData();
		
		// Setup IR data with list in CSV files
		populateIRData(this.irCompanyDataFilePath, IRRenewal_Type.Company);
		populateIRData(this.irIndividualDataFilePath, IRRenewal_Type.Individual);
		populateIRData(this.irPartnersDataFilePath, IRRenewal_Type.Partner);
		populateIRData(this.irPublicBodyDataFilePath, IRRenewal_Type.PublicBody);
		
		// Populate database with data found in list
		populateDBwithIRData();
	}
	
	private void dropAllIRData()
	{
		// Drop any existing IR renewal data
		dao.dropIRData();
		// Remove data list from memory
		irDataList.clear();
	}
	
	private void populateDBwithIRData()
	{
		// Populate database with data found in list
		for (IRData irData : irDataList)
		{
			dao.addIRData(irData);
		}
		log.info("Added " + irDataList.size() + " IR records to the database.");
	}

	/**
	 * Creates a lookup for the IR renewal data based on a CSV Files provided
	 * 
	 * @param csvFile a string of the path to the CSV file
	 * @param irType IRRenewal_Type is the type of IR data provided, as each type has unique data entry
	 */
	private void populateIRData(String csvFile, IRRenewal_Type irType)
	{
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		
		try
		{
			log.info("Try to read from: " + csvFile);
			InputStream is = IRRenewalPopulator.class.getResourceAsStream(csvFile);
			InputStreamReader isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			log.fine("Attempting to read file: " + csvFile);
			int count = 0;
			while ((line = br.readLine()) != null)
			{
				// use comma as separator
				String[] irDataRow = line.split(cvsSplitBy);

				log.fine("IR Data [0: " + irDataRow[0] +
						" , 1: " + irDataRow[1] + 
						" , 2: "+ irDataRow[2] + 
						" , 3: " + irDataRow[3] + "]");
				
				if ("CB_REFERENCE_NUMBER".equalsIgnoreCase(irDataRow[0]))
				{
					// skip this iteration, to ignore CSV headings
					continue;
				}
				count++;
				
				// Updated method
				switch (irType)
				{
				case Company:
					//
					this.irDataList.add(new CompanyIRData(irDataRow));
					break;
				case Individual:
					//
					this.irDataList.add(new IndividualIRData(irDataRow));
					break;
				case Partner:
					//
					this.irDataList.add(new PartnersIRData(irDataRow));
					break;
				case PublicBody:
					//
					this.irDataList.add(new PublicBodyIRData(irDataRow));
					break;
				default:
					break;
				}
				
			}
			log.info("Read: " + count + " irrenewals from " + csvFile + " file.");
			if (count == 0)
			{
				log.severe("Error: Could not find any irdata");
			}
			
		}
		catch (FileNotFoundException e)
		{
			log.info("File not Found: " + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e)
		{
			log.info("IO Exception: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					log.severe("Error: cannot close connection to file, " + e.getMessage());
				}
			}
		}
		log.fine("Done");
	}
	
}
