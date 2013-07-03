/**
 * 
 */
package org.mmarini.hilbert;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author US00852
 * 
 */
public class MessageFormatLog {
	private Log log;

	/**
	 * 
	 */
	public MessageFormatLog(Class<?> clazz) {
		log = LogFactory.getLog(clazz);
	}

	/**
	 * 
	 * @param arg0
	 */
	public void debug(Object arg0) {
		log.debug(arg0);
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
		log.info(arg0);
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
