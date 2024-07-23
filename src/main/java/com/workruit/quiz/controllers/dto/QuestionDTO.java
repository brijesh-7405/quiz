package com.workruit.quiz.controllers.dto;

import java.util.List;

import javax.validation.constraints.Null;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.workruit.quiz.persistence.entity.Question.QuestionType;

import lombok.Data;

/**
 * 
 * @author Santosh
 *
 */
@Data
@JsonIgnoreProperties
public class QuestionDTO {

	public QuestionDTO() {
		super();
	}

	public QuestionDTO(QuestionType questionType, QuestionObjectDTO questionObj, List<QuestionOptionDTO> options) {
		super();
		this.questionType = questionType;
		this.questionObj = questionObj;
		this.options = options;
	}
	@Null
	private Long questionId;
	private QuestionType questionType;
	private QuestionObjectDTO questionObj;
	private List<QuestionOptionDTO> options;
}
