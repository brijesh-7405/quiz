/**
 * 
 */
package com.workruit.quiz.configuration;

/**
 * @author Santosh
 *
 */
public class ConflictException extends Exception {

	private static final long serialVersionUID = -3338510220900380794L;

	public ConflictException(String conflict) {
		super();
		this.conflict = conflict;
	}

	private String conflict;

	public String getConflict() {
		return conflict;
	}

	public void setConflict(String conflict) {
		this.conflict = conflict;
	}
}
