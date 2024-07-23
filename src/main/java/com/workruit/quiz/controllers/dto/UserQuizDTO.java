/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class UserQuizDTO {
	@NotNull
	private Long userQuizId;
	@NotNull
	private Long questionId;
	@NotNull
	private Object answer;
}
