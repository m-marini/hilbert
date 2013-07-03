/**
 * 
 */
package org.mmarini.hilbert;

/**
 * @author US00852
 * 
 */
public interface Rule {

	/**
	 * 
	 */
	public abstract void applyEffect();

	/**
	 * 
	 * @return
	 */
	public abstract boolean isCauseMatched();

}
