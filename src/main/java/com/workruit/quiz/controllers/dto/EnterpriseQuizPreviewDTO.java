/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
@Builder
public class EnterpriseQuizPreviewDTO {
	private EnterpriseDTO enterprise;
	private QuizDetailsDTO quiz;
	private List<QuestionPreviewDTO> questions;
}
