package com.workruit.quiz.controllers.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizStatus;

import lombok.Data;

@Data
public class UserQuizAnalyticsDataDTO {
	private Long quizId;
	private Long enterpriseId;
	private Long userQuizId;
	private String firstName;
	private String lastName;
	private UserQuizStatus status;
	private boolean autoCompleted;
	private String email;
	private String mobile;
	@JsonIgnore
	private Timestamp analyticsTime;
	@JsonIgnore
	private Timestamp postedDate;
	@JsonIgnore
	private Timestamp quizStartedDate;
	private String quizPostedDate;
	private String userQuizStartedDate;
	@JsonInclude(value = Include.NON_NULL)
	private Integer percentage;
	@JsonInclude(value = Include.NON_NULL)
	private Float actualPercentage;
	@JsonInclude(value = Include.NON_NULL)
	private Integer progressPercecntage;
}
