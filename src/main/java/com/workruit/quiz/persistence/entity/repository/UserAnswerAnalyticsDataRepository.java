/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.controllers.dto.UserQuestionAnalyticsDataDTO.AnswerStatus;
import com.workruit.quiz.persistence.entity.UserAnswerAnalyticsData;

/**
 * @author Dell
 *
 */
public interface UserAnswerAnalyticsDataRepository extends JpaRepository<UserAnswerAnalyticsData, Long> {
	/**
	 * 
	 * @param userId
	 * @param quizId
	 * @param questiondId
	 * @return
	 */
	UserAnswerAnalyticsData findByUserIdAndQuizIdAndQuestionId(Long userId, Long quizId, Long questiondId);


	int countByQuizIdAndQuestionIdAndAnswerStatus(Long quizId, Long questionId, AnswerStatus status);

	List<UserAnswerAnalyticsData> findByQuizIdAndQuestionIdAndAnswerStatus(Long quizId, Long questionId,
			AnswerStatus status, Pageable page);
}
