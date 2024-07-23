/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.util.List;

import com.workruit.quiz.constants.QuizVisibility;
import com.workruit.quiz.persistence.entity.Quiz.QuizSubmitStatus;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class UserQuizDetailDTO {
	private Long id;
	private String name;
	private String level;
	private String quizStatus;
	private String deadline;
	private String postedBy;
	private Long quizId;
	private String overallStatus;
	private String overallStatusFloat;
	private String quizPostedDate;
	private Long numberOfParticipants;
	private String category;
	private String topic;
	private QuizVisibility visibility;
	private List<CategoryListDTO> categories;
	private String deadlineDate;
	private QuizSubmitStatus submitStatus;
	private String timeLeftForQuiz = "-1";
	private String timeLimitForQuiz;
	private boolean timedOut;
	private Long enterpriseId;
	private String enterpriseImage;
	private String enterpriseImageKey;
	private String quizSubmittedIn;
}
