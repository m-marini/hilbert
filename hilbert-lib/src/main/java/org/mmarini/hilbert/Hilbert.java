/**
 * 
 */
package org.mmarini.hilbert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author US00852a
 * 
 */
public class Hilbert {
	private static final String DATA_FILENAME = "hilbert.dat";
	private static final int INITIAL_RANDOM_SEED = 123;

	private static Logger log = LoggerFactory.getLogger(Hilbert.class);

	private Society society;
	private PrintWriter dataWriter;

	/**
	 * 
	 */
	public Hilbert() {
	}

	/**
	 * 
	 */
	public void cleanup() {
		if (dataWriter != null) {
			dataWriter.close();
			dataWriter = null;
		}
	}

	/**
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * 
	 */
	public void init() throws SAXException, IOException,
			ParserConfigurationException {
		society = new Society();
		society.setRandom(new Random(INITIAL_RANDOM_SEED));
		try {
			dataWriter = new PrintWriter(DATA_FILENAME);
			society.setDataWriter(dataWriter);
		} catch (FileNotFoundException e) {
			log.error("Error opening file", e);
		}
		society.init();
		log.info("Initialized");
	}

	/**
	 * 
	 * @return
	 */
	public boolean isFinished() {
		return !society.hasPopulation();
	}

	/**
	 * 
	 */
	public void processStep() {
		society.processStep();
	}
}