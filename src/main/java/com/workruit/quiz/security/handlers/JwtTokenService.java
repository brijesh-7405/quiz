/**
 * 
 */
package com.workruit.quiz.security.handlers;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.workruit.quiz.security.utils.JwtTokenSettings;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

/**
 * @author Santosh
 *
 */
@Service
public class JwtTokenService {
	private @Autowired JwtTokenSettings jwtSettings;

	public Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(jwtSettings.getKey()).parseClaimsJws(token).getBody();
	}

	public Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	private Date getExpirationDateFromToken(String token) {
		return getAllClaimsFromToken(token).getExpiration();
	}
}
