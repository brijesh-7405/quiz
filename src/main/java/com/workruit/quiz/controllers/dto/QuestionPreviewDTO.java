/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import com.workruit.quiz.persistence.entity.Question.QuestionType;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class QuestionPreviewDTO {
	private String question;
	private int numberOfOptions;
	private QuestionType type;
}
