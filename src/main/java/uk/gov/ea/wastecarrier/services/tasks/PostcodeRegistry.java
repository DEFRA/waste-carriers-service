package uk.gov.ea.wastecarrier.services.tasks;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * The Postcode Registry determines the X and Y coordinate for a given postcode.
 * 
 * Currently it only have one ability to do this, via a CSV file containing a set of X an Y 
 * coordinates mapped the Outer part of the postcode.
 * 
 * The PostcodeRegistry.POSTCODE_FROM.FILE option must currently be provided.
 * 
 * @author Steve
 *
 */
public class PostcodeRegistry
{

	public ArrayList<String[]> postcodes;
	private static Logger log = Logger.getLogger(PostcodeRegistry.class.getName());
	
	public enum POSTCODE_FROM {
	    FILE, LOOKUP
	}
	
	/**
	 * Setup the Postcode Register
	 * 
	 * @param pf POSTCODE_FROM enumeration, Used to reference the type of postcode lookup
	 * @param csvFile a string of the path to the CSV file
	 */
	public PostcodeRegistry(POSTCODE_FROM pf, String csvFile)
	{
		switch (pf)
		{
			case FILE:
				// Setup Postcode lookup from postcode list in csv file
				setupPostcodesFromFile(csvFile);
				log.info("Lookup MODE FILE.");
				break;
			case LOOKUP:
				// Alternative Setup for future use?
				postcodes = null;
				log.info("Lookup MODE not yet implemented. Please use FILE.");
				break;
		}
	}

	/**
	 * Creates a lookup for the postcode based on a CSV File containing a list of XY coordinates as a reference
	 * 
	 * @param csvFile a string of the path to the CSV file
	 */
	private void setupPostcodesFromFile(String csvFile)
	{
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		
		this.postcodes = new ArrayList<String[]>();

		try
		{
			log.info("Try to read from: " + csvFile);
			InputStream is = PostcodeRegistry.class.getResourceAsStream(csvFile);
			InputStreamReader isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			
			//br = new BufferedReader(new FileReader(csvFile));
			log.fine("Attempting to read file: " + csvFile);
			while ((line = br.readLine()) != null)
			{
				// use comma as separator
				String[] postcodeRow = line.split(cvsSplitBy);

				log.fine("Postcode [id: " + postcodeRow[0] +
						" , name: " + postcodeRow[1] + 
						" , X: "+ postcodeRow[2] + 
						" , Y: " + postcodeRow[3] + "]");
				
				this.postcodes.add(postcodeRow);
			}
			log.info("Read: " + postcodes.size() + " postcodes from postcode file.");
			if (postcodes.size() == 0)
			{
				log.severe("Error: Could not find any postcodes to calculate XY coordinates. Location Population will not function correctly.");
			}
		}
		catch (FileNotFoundException e)
		{
			log.severe("File not Found: " + e.getMessage());
		}
		catch (IOException e)
		{
			log.severe("IO Exception: " + e.getMessage());
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
	
	/**
	 * Gets a set of XY Coordinates for the given postcode provided
	 * 
	 * @param postcode the postcode to search with
	 * @return a Double array containing 2 coordinates, array[0], is the X coordinate, and array[1] is the Y coordinate
	 */
	public Double[] getXYCoords(String postcode) 
	{
		// Covert postcode to Out Code
		String outCode = "";
		if (postcode != null)
		{
			outCode = postcode.split(" ")[0];
		}
		
		// Setup Default XY in-case postcode doesn't exist
		Double[] xyCoords = new Double[2];
		xyCoords[0] = 1.0;
		xyCoords[1] = 1.0;
		
		if (postcodes != null)
		{
			for (String[] s : postcodes)
			{
				if (s[1].equalsIgnoreCase(outCode))
				{
					xyCoords[0] = Double.valueOf(s[2]);
					xyCoords[1] = Double.valueOf(s[3]);
				}
			}
		}
		else
		{
			log.severe("Error: cannot find any postcodes in list. Reterning default values");
		}
		return xyCoords;
	}
}
