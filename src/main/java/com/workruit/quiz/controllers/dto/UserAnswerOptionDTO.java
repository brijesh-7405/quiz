/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.util.HashMap;
import java.util.Map;

import com.workruit.quiz.persistence.entity.Question.QuestionType;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class UserAnswerOptionDTO {
	private Object options;
	private QuestionType type;
	private Map<String, Object> question = new HashMap<>();
}
