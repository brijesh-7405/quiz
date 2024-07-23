/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.QuizAnalyticsData;

/**
 * @author Santosh
 *
 */
public interface QuizAnalyticsDataRepository extends JpaRepository<QuizAnalyticsData, Long> {

	List<QuizAnalyticsData> findByEnterpriseIdOrderByAnalyticsTimeDesc(Long enterpriseId, Pageable pageable);

	void deleteByQuizId(Long quizId);

	Long countByEnterpriseId(long id);

	QuizAnalyticsData findByQuizId(Long quizId);

	List<QuizAnalyticsData> findByEnterpriseId(Long enterpriseId, Pageable pageRequest);

}
