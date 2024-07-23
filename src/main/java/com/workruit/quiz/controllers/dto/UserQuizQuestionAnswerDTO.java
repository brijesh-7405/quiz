/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.util.List;

import com.workruit.quiz.persistence.entity.Question.QuestionType;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class UserQuizQuestionAnswerDTO extends QuestionDTO {
	private Boolean questionAnswered;
	private Object option;

	public UserQuizQuestionAnswerDTO(Boolean questionAnswered) {
		super();
		this.questionAnswered = questionAnswered;
	}

	public UserQuizQuestionAnswerDTO() {
		super();
	}

	public UserQuizQuestionAnswerDTO(QuestionType questionType, QuestionObjectDTO questionObj,
			List<QuestionOptionDTO> options) {
		super(questionType, questionObj, options);
	}
}
