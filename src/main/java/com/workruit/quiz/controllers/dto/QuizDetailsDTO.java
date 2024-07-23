/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.workruit.quiz.constants.QuizVisibility;
import com.workruit.quiz.persistence.entity.Quiz.QuizLevel;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class QuizDetailsDTO {
	@Null
	private Long id;
	@NotNull
	private long enterpriseId;
	@NotNull
	private QuizLevel level;
	private String targetAudience;
	private String code;
	@NotNull
	private String expiryDate;
	@NotNull
	@Size(min = 1)
	private String name;
	private List<CategoryDTO> categories;
	private List<CategoryDTO> topics;
	private QuizVisibility visibility;
	private String quizTimeLimit;
}
