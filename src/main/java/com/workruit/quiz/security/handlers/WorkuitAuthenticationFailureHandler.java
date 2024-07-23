/**
 * 
 */
package com.workruit.quiz.security.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * @author Santosh
 *
 */
@Component
public class WorkuitAuthenticationFailureHandler implements AuthenticationFailureHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.security.web.authentication.AuthenticationFailureHandler#
	 * onAuthenticationFailure(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse,
	 * org.springframework.security.core.AuthenticationException)
	 */
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.getWriter().write(exception.getMessage());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
	}
}
