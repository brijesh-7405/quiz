/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.sql.Timestamp;

import com.workruit.quiz.controllers.dto.UserQuestionAnalyticsDataDTO.AnswerStatus;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class UserQuizQuestionsResponse  {
	private String question;
	private AnswerStatus status;
	private Long questionId;
	private Timestamp questionCreatedDate;

}
