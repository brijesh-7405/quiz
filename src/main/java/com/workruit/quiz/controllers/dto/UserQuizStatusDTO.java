/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class UserQuizStatusDTO {
	private Long userQuizId;
	private Long correct;
	private Long inCorrect;
	private Long answered;
	private Long skipped;
	private Long inReview;
	private Long numberOfQuestions;
	private Long unansweredCount;
	private String quizSubmittedIn;
	private Long likeDislikeCount;
}
