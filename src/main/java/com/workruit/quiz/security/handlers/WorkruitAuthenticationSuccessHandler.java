/**
 * 
 */
package com.workruit.quiz.security.handlers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workruit.quiz.security.utils.JwtTokenSettings;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * @author Santosh
 *
 */
@Component
public class WorkruitAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private ObjectMapper objectMapper;
	private JwtTokenSettings jwtSettings;
	private static final Logger logger = LoggerFactory.getLogger(WorkruitAuthenticationSuccessHandler.class);

	public WorkruitAuthenticationSuccessHandler(ObjectMapper objectMapper, JwtTokenSettings jwtSettings) {
		super();
		this.objectMapper = objectMapper;
		this.jwtSettings = jwtSettings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.security.web.authentication.AuthenticationSuccessHandler#
	 * onAuthenticationSuccess(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse,
	 * org.springframework.security.core.Authentication)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onAuthenticationSuccess(HttpServletRequest arg0, HttpServletResponse arg1, Authentication arg2)
			throws IOException, ServletException {
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) arg2;
		Map<String, Object> userDetails = new HashMap<>();
		userDetails.put("username", usernamePasswordAuthenticationToken.getPrincipal());
				
		Claims claims = Jwts.claims(userDetails);
		claims.setId(usernamePasswordAuthenticationToken.getPrincipal().toString());
		String token = getAccessToken(claims);
		Map<String, String> map = new HashMap<>();		
		map.put("access_token", token);
		map.put("iat", claims.getIssuedAt().toLocaleString());
		// map.put("eat", claims.getExpiration().toLocaleString());
		arg1.setStatus(HttpStatus.OK.value());
		logger.info("Logging the token: " + objectMapper.writeValueAsString(map));
		arg1.getWriter().write(objectMapper.writeValueAsString(map));
		arg1.setContentType(MediaType.APPLICATION_JSON_VALUE);

	}

	public String getAccessToken(Claims claims) {
		LocalDateTime localDateTime = LocalDateTime.now();
		String token = Jwts.builder().setClaims(claims).setIssuer(jwtSettings.getTokenIssuer())
				.setIssuedAt(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()))
				.setExpiration(
						Date.from(localDateTime.plusMinutes(Integer.parseInt(jwtSettings.getTokenExpirationTime()))
								.atZone(ZoneId.systemDefault()).toInstant()))
				.signWith(SignatureAlgorithm.RS512, jwtSettings.getKey()).compact();
		return token;
	}

}
