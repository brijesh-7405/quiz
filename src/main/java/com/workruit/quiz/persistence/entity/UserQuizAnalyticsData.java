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

import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizStatus;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Entity
@Table(name = "user_quiz_analytics_data")
@Data
public class UserQuizAnalyticsData extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private Long id;
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "quiz_id")
	private Long quizId;
	@Column(name = "enterprise_id")
	private Long enterpriseId;
	@Column(name = "userquiz_id")
	private Long userQuizId;
	@Column(name = "first_name")
	private String firstName;
	@Column(name = "last_name")
	private String lastName;
	@Column(name = "quiz_status")
	@Enumerated(EnumType.STRING)
	private UserQuizStatus status;
	@Column(name = "email")
	private String email;
	@Column(name = "mobile")
	private String mobile;
	@Column(name = "analytics_time")
	private Timestamp analyticsTime;
	@Column(name = "posted_date")
	private Timestamp postedDate;
	@Column(name = "quiz_started_date")
	private Timestamp quizStartedDate;
	@Column(name = "percentage")
	private Integer percentage = -1;
	@Column(name = "visible")
	private boolean visible = true;
	@Column(name = "auto_completed")
	private boolean autoCompleted;

	public UserQuizAnalyticsData() {
		super();
	}

	public UserQuizAnalyticsData(Long quizId, Long enterpriseId, Long userQuizId, UserQuizStatus status,
			Timestamp postedDate, Timestamp quizStartedDate) {
		super();
		this.quizId = quizId;
		this.enterpriseId = enterpriseId;
		this.userQuizId = userQuizId;
		this.status = status;
		this.postedDate = postedDate;
		this.quizStartedDate = quizStartedDate;
	}

}
