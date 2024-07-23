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

import com.workruit.quiz.persistence.entity.Quiz.QuizLevel;
import com.workruit.quiz.persistence.entity.Quiz.QuizSubmitStatus;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Table(name = "quiz_analytics_data")
@Entity
@Data
public class QuizAnalyticsData extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private Long id;
	@Column(name = "enterprise_id")
	private Long enterpriseId;
	@Column(name = "enterprise_name")
	private String enterpriseName;
	@Column(name = "quiz_id")
	private Long quizId;
	@Column(name = "quiz_name")
	private String quizName;
	@Column(name = "how_many_took_quiz")
	private Long howManyTookQuiz = 0L;
	@Column(name = "how_many_inprogress_quiz")
	private Long howManyInprogressQuiz = 0L;
	@Column(name = "how_many_inreview_quiz")
	private Long howManyInReviewQuiz = 0L;
	@Column(name = "how_many_notstarted_quiz")
	private Long howManyYetToTakeQuiz = 0L;
	@Column(name = "how_many_completed_quiz")
	private Long howManyCompletedQuiz = 0L;
	@Column(name = "how_many_timedout_quiz")
	private Long howManyTimedOutQuiz = 0L;
	@Column(name = "analytics_time")
	private Timestamp analyticsTime;
	@Column(name = "number_of_questions")
	private Long numberOfQuestions;
	@Column(name = "topic")
	private String topic;
	@Column(name = "category")
	private String category;
	@Column(name = "expiry_date")
	private Date expiryDate;
	@Column(name = "level")
	private QuizLevel level;
	@Column(name = "posted_date")
	private Timestamp postedDate;
	@Column(name = "quiz_status")
	@Enumerated(value = EnumType.STRING)
	private QuizSubmitStatus quizStatus;
	@Column(name = "target_audience")
	private String targetAudience;
	@Column(name = "code")
	private String code;
	@Column(name = "categories")
	private String categories;
	@Column(name = "activated_date")
	private Timestamp activatedDate;
	@Column(name = "review_date")
	private Timestamp reviewDate;
	@Column(name = "closed_date")
	private Timestamp closedDate;
	@Column(name = "rejected_date")
	private Timestamp rejectedDate;
	@Column(name = "quiz_time_limit")
	private String quizTimeLimit;

}
