package com.workruit.quiz.security.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.workruit.quiz.controllers.dto.FailedMessage;
import com.workruit.quiz.controllers.dto.Message;

public class ControllerUtils {

	@SuppressWarnings("rawtypes")
	public static ResponseEntity genericErrorMessage() {
		FailedMessage failedMessage = FailedMessage.builder()
				.msg(Message.builder().description("Internal Server Error").title("Failed").build()).status("Failed")
				.build();
		return new ResponseEntity<>(failedMessage, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@SuppressWarnings("rawtypes")
	public static ResponseEntity customErrorMessage(String description) {
		FailedMessage failedMessage = FailedMessage.builder()
				.msg(Message.builder().description(description).title("Failed").build()).status("Failed").build();
		return new ResponseEntity<>(failedMessage, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
