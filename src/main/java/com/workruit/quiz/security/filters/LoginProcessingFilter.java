/**
 * 
 */
package com.workruit.quiz.security.filters;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Santosh
 *
 */
public class LoginProcessingFilter extends AbstractAuthenticationProcessingFilter {
	private AuthenticationSuccessHandler authenticationSuccessHandler;
	private AuthenticationFailureHandler authenticationFailureHandler;
	private ObjectMapper objectMapper;

	public LoginProcessingFilter(String defaultFilterProcessesUrl,
			AuthenticationSuccessHandler authenticationSuccessHandler,
			AuthenticationFailureHandler authenticationFailureHandler, ObjectMapper objectMapper) {
		super(defaultFilterProcessesUrl);
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		this.authenticationFailureHandler = authenticationFailureHandler;
		this.objectMapper = objectMapper;
	}

/*	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		if (!requiresAuthentication(request, response)) {
			chain.doFilter(req, res);
			return;
		}
		try {
			Authentication auth = attemptAuthentication(request, response);
			successfulAuthentication(request, response, chain, auth);
			return;
		} catch (Exception e) {
			unsuccessfulAuthentication(request, response, null);
			return;
		}

	}*/

	@SuppressWarnings("unchecked")
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException {
		Map<String, String> loginRequest = objectMapper.readValue(request.getReader(), Map.class);
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				loginRequest.get("username"), loginRequest.get("password"));
		usernamePasswordAuthenticationToken.setDetails(loginRequest.get("details"));
		Authentication authentication = getAuthenticationManager().authenticate(usernamePasswordAuthenticationToken);
		return authentication;
	}

	@Override
	public void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		authenticationSuccessHandler.onAuthenticationSuccess(request, response, authResult);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		authenticationFailureHandler.onAuthenticationFailure(request, response, failed);
	}
}
