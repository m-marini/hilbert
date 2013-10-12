/**
 * 
 */
package org.mmarini.hilbert;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author US00852
 * 
 */
public class MessageFormatLog {
	private Logger log;

	/**
	 * 
	 */
	public MessageFormatLog(Class<?> clazz) {
		log = LoggerFactory.getLogger(clazz);
	}

	/**
	 * 
	 * @param arg0
	 */
	public void debug(Object arg0) {
		log.debug(String.valueOf(arg0));
	}

	/**
	 * 
	 * @param arg0
	 */
	public void debug(String message, Object... parms) {
		log.debug(MessageFormat.format(message, parms));
	}

	/**
	 * 
	 * @param arg0
	 */
	public void info(Object arg0) {
		log.info(String.valueOf(arg0));
	}

	/**
	 * 
	 * @param message
	 * @param parms
	 */
	public void info(String message, Object... parms) {
		log.info(MessageFormat.format(message, parms));
	}

}
