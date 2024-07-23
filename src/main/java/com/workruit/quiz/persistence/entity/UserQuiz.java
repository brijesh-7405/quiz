/**
 * 
 */
package com.workruit.quiz.persistence.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Table(name = "user_quiz")
@Entity
@Data
public class UserQuiz extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private Long id;
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "quiz_id")
	private Long quizId;
	@Column(name = "quiz_status")
	@Enumerated(EnumType.STRING)
	private UserQuizStatus status;
	@Column(name = "quiz_result")
	@Enumerated(EnumType.STRING)
	private UserQuizResult result;
	@Column(name = "result_data")
	private Integer resultData;
	@Column(name = "result_data_float")
	private Float resultDataFloat;
	@Column(name = "result_content")
	private String resultContent;
	@Column(name = "visible")
	private boolean visible = false;
	@Column(name = "quiz_start_time")
	private Timestamp quizStartTime;
	@Column(name = "quiz_timedout")
	private boolean quizTimedOut;
	@Column(name = "quiz_submit_time")
	private String quizSubmittedIn;

	public static enum UserQuizStatus {
		NOT_STARTED, IN_PROGRESS, PENDING_REVIEW, COMPLETED, ERROR
	}

	public static enum UserQuizResult {
		PASSED, FAILED
	}
}
