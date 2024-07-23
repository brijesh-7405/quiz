/**
 * 
 */
package com.workruit.quiz.persistence.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.workruit.quiz.persistence.entity.Question.QuestionType;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Table(name = "question_analytics_data")
@Entity
@Data
public class QuestionAnalyticsData extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private Long id;
	@Column(name = "question_id")
	private Long questionId;
	@Column(name = "question_type")
	private QuestionType questionType;
	@Column(name = "question")
	private String question;
	@Column(name = "answer")
	private String answer;
	@Column(name = "correct_count")
	private Long correctCount;
	@Column(name = "in_correctcount")
	private Long inCorrectCount;
	@Column(name = "inreview_count")
	private Long inReviewCount;
	@Column(name = "unanswered_count")
	private Long unAnsweredCount;
	@Column(name = "analytics_time")
	private Timestamp analyticsTime;
	@Column(name = "quiz_id")
	private Long quizId;
	@Column(name = "like_count")
	private Long likeCount;
	@Column(name = "dislike_count")
	private Long dislikeCount;
}
