package com.workruit.quiz.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.workruit.quiz.controllers.dto.UserQuestionAnalyticsDataDTO.AnswerStatus;

import lombok.Data;

@Table(name = "user_answer_analytics_data")
@Entity
@Data
public class UserAnswerAnalyticsData extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private Long id;
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "userquiz_id")
	private Long userQuizId;
	@Column(name = "quiz_id")
	private Long quizId;
	@Column(name = "question_id")
	private Long questionId;
	@Column(name = "answer_status")
	private AnswerStatus answerStatus;
}
