package com.workruit.quiz.controllers;

import java.util.Date;
import java.util.List;

import lombok.Data;

public class ExceptionResponse {
	private Date timestamp;
	private String message;
	private List<FieldError> details;

	public ExceptionResponse(Date timestamp, String message, List<FieldError> details) {
		super();
		this.timestamp = timestamp;
		this.message = message;
		this.details = details;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getMessage() {
		return message;
	}

	public List<FieldError> getDetails() {
		return details;
	}

	@Data
	public static class FieldError {
		public FieldError(String name, String message) {
			this.name = name;
			this.message = message;
		}

		private String name;
		private String message;
	}
}