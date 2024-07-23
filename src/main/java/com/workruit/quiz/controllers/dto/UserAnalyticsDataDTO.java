package com.workruit.quiz.controllers.dto;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.workruit.quiz.persistence.entity.Quiz.QuizSubmitStatus;
import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAnalyticsDataDTO {
	private String firstName;
	private String lastName;
	private UserQuizStatus status;
	private QuizSubmitStatus quizStatus;
	private Long quizId;
	private String quizName;
	private String level;
	private Long userQuizId;
	private String email;
	private String mobile;
	private String location;
	private String college;
	@JsonIgnore
	private Timestamp analyticsTime;
	private String expiryDate;
	private String startedDate;
	@JsonInclude(value = Include.NON_NULL)
	private Integer percentage;
	@JsonInclude(value = Include.NON_NULL)
	private Float actualPercentage;
	@JsonInclude(value = Include.NON_NULL)
	private Integer progressPercentage;
	@JsonInclude(value = Include.NON_NULL)
	private Float actualProgressPercentage;
	private String category;
	private String topic;
	@JsonInclude(value = Include.NON_NULL)
	private List<CategoryListDTO> userInterests;
}
