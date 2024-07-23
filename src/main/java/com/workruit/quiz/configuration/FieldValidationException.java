/**
 * 
 */
package com.workruit.quiz.configuration;

import java.util.List;

import com.workruit.quiz.controllers.ExceptionResponse.FieldError;

/**
 * @author Santosh
 *
 */
public class FieldValidationException extends Exception {

	private static final long serialVersionUID = 1L;
	private List<FieldError> fieldErrors;

	public FieldValidationException(List<FieldError> fieldErrors) {
		super();
		this.fieldErrors = fieldErrors;
	}

	public List<FieldError> getFieldErrors() {
		return fieldErrors;
	}

	public void setFieldErrors(List<FieldError> fieldErrors) {
		this.fieldErrors = fieldErrors;
	}

}
