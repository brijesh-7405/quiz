package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizStatus;
import com.workruit.quiz.persistence.entity.UserQuizAnalyticsData;

public interface UserQuizAnalyticsDataRepository extends JpaRepository<UserQuizAnalyticsData, Long> {

	Long countByEnterpriseId(Long enterpriseId);

	List<UserQuizAnalyticsData> findByQuizIdAndVisible(Long quizId, boolean visible, Pageable newPAgeRequest);

	List<UserQuizAnalyticsData> findByQuizIdAndVisibleAndEnterpriseId(Long quizId, boolean visible, Long enterpriseId,
			Pageable newPAgeRequest);

	List<UserQuizAnalyticsData> findByUserId(Long userId, Pageable newPAgeRequest);

	Long countByStatusAndQuizIdAndPercentageGreaterThanEqualAndPercentageLessThan(UserQuizStatus completed, Long quizId,
			int i, int j);

	Long countByQuizId(Long quizId);

	Long countByQuizIdAndVisible(Long quizId, boolean visible);

	Long countByQuizIdAndVisibleAndEnterpriseId(Long quizId, boolean visible, Long enterpriseId);

	Long countByStatusAndQuizId(UserQuizStatus status, Long quizId);

	Long countByStatusAndQuizIdAndEnterpriseId(UserQuizStatus status, Long quizId, Long enterpriseId);

	Long countByStatusInAndQuizIdAndEnterpriseIdAndAutoCompleted(List<UserQuizStatus> status, Long quizId, Long enterpriseId,
			boolean autoCompleted);

	Long countByUserId(Long userId);

	List<UserQuizAnalyticsData> findByEnterpriseId(Long enterpriseId, Pageable newPAgeRequest);

	List<UserQuizAnalyticsData> findByStatusAndQuizId(UserQuizStatus status, Long quizId, Pageable newPAgeRequest);

	List<UserQuizAnalyticsData> findByStatusAndQuizIdAndEnterpriseIdAndAutoCompleted(UserQuizStatus status, Long quizId,
			Long enterpriseId, boolean autoCompleted, Pageable newPAgeRequest);

	List<UserQuizAnalyticsData> findByStatusInAndQuizIdAndEnterpriseIdAndAutoCompleted(List<UserQuizStatus> status, Long quizId,
																					 Long enterpriseId, boolean autoCompleted, Pageable newPAgeRequest);

	List<UserQuizAnalyticsData> findByStatusAndQuizIdAndEnterpriseId(UserQuizStatus status, Long quizId,
			Long enterpriseId, Pageable newPAgeRequest);

	void deleteByUserQuizId(Long userQuizId);

	List<UserQuizAnalyticsData> findByUserId(Long userId);

}