package com.workruit.quiz.controllers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
public class UserQuestionAnalyticsDataDTO {
	private Long userQuizId;
	private Long questionId;
	@JsonInclude(Include.NON_NULL)
	private Long userId;
	private AnswerStatus answerStatus;
	private String firstName;
	private String lastName;
	private String email;
	private String mobile;
	private String quizStartedDate;
	private Long quizId;
	private String quizName;
	private Integer correctPercentage;
	private Integer inCorrectPercentage;
	@JsonInclude(Include.NON_NULL)
	private String userAnswer;

	public static enum AnswerStatus {
		CORRECT, INCORRECT, INREVIEW, NOT_ANSWERED, LIKE, DISLIKE
	}
}
