/**
 * 
 */
package com.workruit.quiz.persistence.entity;

import java.sql.Timestamp;
import java.util.Date;

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
@Table(name = "quiz")
@Entity
@Data
public class Quiz extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private Long id;
	@Column(name = "enterprise_id")
	private Long enterpriseId;
	@Column(name = "level")
	@Enumerated(EnumType.STRING)
	private QuizLevel level;
	@Column(name = "expiry_date")
	private Date expiryDate;
	@Column(name = "target_audience")
	private String targetAudience;
	@Column(name = "code")
	private String code;
	@Column(name = "name")
	private String name;
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private QuizSubmitStatus status;
	@Column(name = "comments")
	private String comments;
	@Column(name = "activated_date")
	private Timestamp activatedDate;
	@Column(name = "review_date")
	private Timestamp reviewDate;
	@Column(name = "closed_date")
	private Timestamp closedDate;
	@Column(name = "rejected_date")
	private Timestamp rejectedDate;
	@Column(name = "uuid")
	private String uuid;
	@Column(name = "quiz_time_limit")
	private String quizTimeLimit = "01:00:00";

	public enum QuizLevel {
		BASIC, INTERMEDIATE, EXPERT, ADVANCED
	}

	public enum QuizStatus {
		NOT_STARTED, IN_PROGRESS, COMPLETED, ERROR
	}

	public enum QuizResult {
		// Dont change the order of the enums
		PASSED, FAILED, NONE
	}

	public enum QuizSubmitStatus {
		PENDING, REVIEW, ACTIVE, REJECT, CLOSE
	}
}
