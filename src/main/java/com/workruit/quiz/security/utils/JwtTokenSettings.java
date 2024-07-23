/**
 * 
 */
package com.workruit.quiz.security.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@ConfigurationProperties(prefix="workruit.authentication")
public class JwtTokenSettings {
	private static final Logger logger = LoggerFactory.getLogger(JwtTokenSettings.class);
	@Value("${key.file.path}")
	private String keyFilePath;
	private @Value("${key.token.password}") String tokenKey;
	private @Value("tokenExpirationTime") String tokenExpirationTime;
	private @Value("tokenIssuer") String tokenIssuer;
	private @Value("${key.token.alias}") String tokenAlias;
	private byte[] tokenSigningKey;
	private Key key;

	@PostConstruct
	public void init() {
		try {
			
			ClassPathResource inputStream = new ClassPathResource(keyFilePath);
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(inputStream.getInputStream(), tokenKey.toCharArray());
			this.key = keystore.getKey(tokenAlias, tokenKey.toCharArray());
		} catch (UnrecoverableKeyException e) {
			logger.error(e.getMessage());
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (KeyStoreException e) {
			logger.error(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage());
		} catch (CertificateException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public String getTokenIssuer() {
		return tokenIssuer;
	}

	public void setTokenIssuer(String tokenIssuer) {
		this.tokenIssuer = tokenIssuer;
	}

	public String getTokenKey() {
		return tokenKey;
	}

	public void setTokenKey(String tokenKey) {
		this.tokenKey = tokenKey;
	}

	public String getTokenExpirationTime() {
		return tokenExpirationTime;
	}

	public void setTokenExpirationTime(String tokenExpirationTime) {
		this.tokenExpirationTime = tokenExpirationTime;
	}

	public byte[] getTokenSigningKey() {
		return tokenSigningKey;
	}

	public void setTokenSigningKey(byte[] tokenSigningKey) {
		this.tokenSigningKey = tokenSigningKey;
	}

	public Key getKey() {
		if (key == null) {
			init();
		}
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}
}
