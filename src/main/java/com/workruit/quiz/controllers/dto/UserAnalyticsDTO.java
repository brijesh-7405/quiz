/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Santosh
 *
 */
@Getter
@Setter
public class UserAnalyticsDTO {
	private Long id;
	private String firstName;
	private String lastName;
	private String email;
	private String location;
	private String mobile;
	private String college;
	private String company;
	@JsonIgnore
	private Timestamp createdDate;
	private String userRegistrationDate;
	private Long quizsTaken;
}
