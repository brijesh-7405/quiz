/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.workruit.quiz.persistence.entity.Quiz.QuizLevel;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class QuizDTO {
	@Null
	private Long id;
	@NotNull
	@Size(min = 1)
	private List<Long> categoryIds;
	@NotNull
	@Size(min = 1)
	private List<Long> topicIds;
	@NotNull
	private long enterpriseId;
	@NotNull
	private QuizLevel level;
	private String targetAudience;
	@Size(min = 6, max=8, message = "Code should be of minimum length of 6 and maximum 8")
	private String code;
	@NotNull
	private String expiryDate;
	@NotNull
	@Size(min = 2, max = 300, message = "Quiz length should be less than 300 characters")
	private String name;
	@JsonInclude(value = Include.NON_NULL)
	private List<CategoryListDTO> categories;
	private String uuid;
	private String quizTimeLimit;
	private String enterpriseLogo;
}
