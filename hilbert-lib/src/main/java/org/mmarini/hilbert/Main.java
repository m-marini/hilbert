/**
 * 
 */
package org.mmarini.hilbert;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * @author US00852
 * 
 */
public class Main {

	private static final int MAX_ITERATION_COUNT = 10000;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Throwable {
		new Main().run();
	}

	private Hilbert handler;

	/**
	 * 
	 */
	public Main() {
		handler = new Hilbert();
	}

	/**
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * 
	 */
	private void run() throws SAXException, IOException,
			ParserConfigurationException {
		handler.init();
		for (int i = 0; i < MAX_ITERATION_COUNT && !handler.isFinished(); ++i) {
			handler.processStep();
		}
		handler.cleanup();
	}
}
