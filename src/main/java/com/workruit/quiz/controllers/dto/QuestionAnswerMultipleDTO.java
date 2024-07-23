/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.util.List;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class QuestionAnswerMultipleDTO {
	private Long quizId;
	private List<QuestionAnswerDTO> questionAnswers;
}
