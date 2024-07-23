package com.workruit.quiz.controllers.dto;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.workruit.quiz.persistence.entity.Quiz.QuizLevel;
import com.workruit.quiz.persistence.entity.Quiz.QuizSubmitStatus;

import lombok.Data;

@Data
public class QuizAnalyticsDTO {
	private EnterpriseDetailsDTO enterprise;
	private Long quizId;
	private String quizName;
	private Long howManyTookQuiz;
	private Long howManyInprogressQuiz;
	private Long howManyInReviewQuiz;
	private Long howManyYetToTakeQuiz;
	private Long howManyCompletedQuiz;
	private Long howManyTimedOutQuiz;
	@JsonIgnore
	private Timestamp analyticsTime;
	private Long numberOfQuestions;
	private String topic;
	private String category;
	@JsonIgnore
	private Date expiryDate;
	private String quizExpiryDate;
	private QuizLevel level;
	private String code;
	private String quizCreatedDate;
	@JsonIgnore
	private Date createdDate;
	private QuizSubmitStatus quizStatus;
	private String targetAudience;
	@JsonInclude(value = Include.NON_NULL)
	private List<CategoryListDTO> categories;
	private String quizTimeLimit;
}
