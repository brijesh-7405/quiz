/**
 * 
 */
package com.workruit.quiz.persistence.entity;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
@JsonInclude(Include.NON_NULL)
public class UserQuizStatusContent {
	private long numberOfQuestions;
	private long correctAnswersCount;
	private long inCorrectAnswersCount;
	private long reviewAnswersCount;
	private long unansweredCount;
	private Map<Long, Boolean> answersStatus;
	private long likeDislikeCount;
	private long likeCount;
	private long disLikeCount;
}
