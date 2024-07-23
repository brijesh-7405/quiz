/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizResult;
import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizStatus;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class UserQuizResponseDTO {
	private Long userQuizId;
	private UserQuizStatus status;
	private UserQuizResult quizResult;
	private Integer result;
	private Float floatResult;
}
