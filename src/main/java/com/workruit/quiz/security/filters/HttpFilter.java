/**
 * 
 */
package com.workruit.quiz.security.filters;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import com.workruit.quiz.controllers.dto.UserDetailsDTO;
import com.workruit.quiz.persistence.entity.UserExpiredToken;
import com.workruit.quiz.persistence.entity.repository.UserExpiredTokenRepository;
import com.workruit.quiz.security.handlers.JwtTokenService;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Santosh
 *
 */
@Component
@WebFilter
@Slf4j
public class HttpFilter extends GenericFilterBean {
	private @Autowired JwtTokenService jwtTokenService;
	private @Autowired UserExpiredTokenRepository userExpiredTokenRepository;
	private Set<String> ignoreURLs = new HashSet<>();

	@PostConstruct
	public void init() {
		ignoreURLs.add("/swagger-ui.html");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) req;
		if (ignoreURLs.contains(httpServletRequest.getRequestURI())
				|| httpServletRequest.getRequestURI().startsWith("/webjars")
				|| httpServletRequest.getRequestURI().startsWith("/swagger-resources")
				|| httpServletRequest.getRequestURI().startsWith("/v2/api-docs")
				|| httpServletRequest.getRequestURI().startsWith("/swagger.json")
				|| httpServletRequest.getRequestURI().startsWith("/signup")
				|| httpServletRequest.getRequestURI().startsWith("/signup/social")
				|| httpServletRequest.getRequestURI().startsWith("/verify")
				|| httpServletRequest.getRequestURI().startsWith("/sendOTP")
				|| httpServletRequest.getRequestURI().startsWith("/facebook/callback")
				|| httpServletRequest.getRequestURI().startsWith("/payment/callback")
				|| httpServletRequest.getRequestURI().startsWith("/get-sms-code")
				|| httpServletRequest.getRequestURI().startsWith("/payment/callback/auto_renew")
				|| httpServletRequest.getRequestURI().startsWith("/payment/generatepaymentlink")
				|| httpServletRequest.getRequestURI().startsWith("/payment/autorenew/confirmation")
				|| httpServletRequest.getRequestURI().startsWith("/payment/custom_order")) {
			chain.doFilter(req, res);
			return;
		}
		String header = httpServletRequest.getHeader("Authorization");
		if (httpServletRequest.getRequestURI().startsWith("/user-logout")) {
			String[] tokenArray = header.split(" ");
			Claims claims = jwtTokenService.getAllClaimsFromToken(tokenArray[1]);
			if (claims != null) {
				LinkedHashMap user = (LinkedHashMap) claims.get("username");
				UserExpiredToken userExpiredToken = new UserExpiredToken();
				userExpiredToken.setUserId(Long.parseLong(user.get("id").toString()));
				userExpiredToken.setAccessToken(tokenArray[1]);
				userExpiredTokenRepository.save(userExpiredToken);
			}
		}

		if (header != null && header.startsWith("Bearer")) {
			String[] tokenArray = header.split(" ");
			Claims claims = null;
			try {
				boolean tokenExpired = jwtTokenService.isTokenExpired(tokenArray[1]);
				if (tokenExpired) {
					HttpServletResponse response = (HttpServletResponse) res;
					response.setStatus(HttpStatus.UNAUTHORIZED.value(), "Token is Expired");
					return;
				}
				claims = jwtTokenService.getAllClaimsFromToken(tokenArray[1]);
			} catch (Exception e) {
				HttpServletResponse response = (HttpServletResponse) res;
				response.setStatus(HttpStatus.UNAUTHORIZED.value(), "Expired Token or Token is malformed");
				return;
			}
			if (claims != null) {
				LinkedHashMap user = (LinkedHashMap) claims.get("username");
				UserDetailsDTO userDetailsDTO = new UserDetailsDTO();
				userDetailsDTO.setEmail(user.get("email").toString());
				userDetailsDTO.setUserRole(user.get("userRole").toString());
				if (user.get("enterpriseId") != null) {
					userDetailsDTO.setEnterpriseId(Long.parseLong(user.get("enterpriseId").toString()));
				}
				long userId = Long.parseLong(user.get("id").toString());
				if (userExpiredTokenRepository.countByUserIdAndAccessToken(userId, tokenArray[1]) > 0) {
					HttpServletResponse response = (HttpServletResponse) res;
					response.setStatus(HttpStatus.UNAUTHORIZED.value(), "Token is an Expired token");
					return;
				}
				userDetailsDTO.setId(userId);
				Authentication authentication = new UsernamePasswordAuthenticationToken(userDetailsDTO, null);
				SecurityContextHolder.getContext().setAuthentication(authentication);
				chain.doFilter(req, res);
				return;
			} else {
				HttpServletResponse response = (HttpServletResponse) res;
				response.setStatus(HttpStatus.UNAUTHORIZED.value(), "Token is invalid");
				return;
			}
		} else {
			log.warn("401 Unauthorized in httpfilter");
			HttpServletResponse response = (HttpServletResponse) res;
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			return;
		}
	}
}
