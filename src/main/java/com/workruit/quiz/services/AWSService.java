/**
 * 
 */
package com.workruit.quiz.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * @author Dell
 *
 */
@Component
public class AWSService {

	@Value("${aws.s3.clientId}")
	private String clientId;
	@Value("${aws.s3.clientSecret}")
	private String clientSecret;

	public AmazonS3 getS3Client() {
		AWSCredentials credentials = new BasicAWSCredentials(clientId, clientSecret);
		AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_2)
				.withCredentials(credentialsProvider).build();
		return s3Client;
	}
}
