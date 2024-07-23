/**
 * 
 */
package com.workruit.quiz.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.workruit.quiz.configuration.ConflictException;
import com.workruit.quiz.configuration.FieldValidationException;
import com.workruit.quiz.configuration.WorkruitAuthorizationException;

/**
 * @author Santosh
 *
 */
@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		List<com.workruit.quiz.controllers.ExceptionResponse.FieldError> errors = new ArrayList<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errors.add(new com.workruit.quiz.controllers.ExceptionResponse.FieldError(fieldError.getField(),
					fieldError.getDefaultMessage()));
		}
		ExceptionResponse errorDetails = new ExceptionResponse(new Date(), "Validation Failed", errors);
		return new ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(FieldValidationException.class)
	public final ResponseEntity<Object> handleUserNotFoundException(FieldValidationException ex, WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(), ex.getFieldErrors());
		return new ResponseEntity(exceptionResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ConflictException.class)
	public final ResponseEntity<Object> handleConflictException(ConflictException ex, WebRequest request) {
		return new ResponseEntity(ex.getMessage(), HttpStatus.CONFLICT);
	}

	@ExceptionHandler(WorkruitAuthorizationException.class)
	public final ResponseEntity<Object> handleAuthorizationException(WorkruitAuthorizationException ex,
			WebRequest request) {
		return new ResponseEntity("User doesnt have sufficient permission", HttpStatus.UNAUTHORIZED);
	}

}