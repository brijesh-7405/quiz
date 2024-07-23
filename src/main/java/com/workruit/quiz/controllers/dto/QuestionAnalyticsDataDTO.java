/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.workruit.quiz.persistence.entity.Question.QuestionType;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author Santosh
 *
 */
@Data
public class QuestionAnalyticsDataDTO {
	@JsonInclude(value = Include.NON_NULL)
	private Long id;
	private Long questionId;
	private QuestionType questionType;
	private Object question;
	private Object answer;
	@JsonInclude(value = Include.NON_NULL)
	private Long correctCount = 0L;
	@JsonInclude(value = Include.NON_NULL)
	private Long inCorrectCount = 0L;
	@JsonInclude(value = Include.NON_NULL)
	private Long inReviewCount = 0L;
	@JsonInclude(value = Include.NON_NULL)
	private Long unAnsweredCount = 0L;
	@JsonIgnore
	private Timestamp analyticsTime;
	private Long quizId;
	@JsonInclude(value = Include.NON_NULL)
	private Long likeCount = 0L;
	@JsonInclude(value = Include.NON_NULL)
	private Long dislikeCount = 0L;
}
