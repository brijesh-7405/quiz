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
public class EnterpriseDetails {
	private EnterpriseDTO enterprise;
	private List<QuizDTO> quizs;
}
