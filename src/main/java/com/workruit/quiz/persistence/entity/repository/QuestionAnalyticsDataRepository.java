/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.workruit.quiz.persistence.entity.QuestionAnalyticsData;

/**
 * @author Santosh
 *
 */
public interface QuestionAnalyticsDataRepository extends JpaRepository<QuestionAnalyticsData, Long> {

	void deleteByQuizId(Long quizId);

	List<QuestionAnalyticsData> findByQuizId(Long quizId, Pageable of);

	@Query(nativeQuery = true, value = "select sum(correct_count) from question_analytics_data qad, quiz q "
			+ "where question_id=:questionId and q.status in ('ACTIVE','CLOSE') and q.id = qad.quiz_id and q.id = :quizId")
	int countCorrectQuestionsAnswers(@Param("quizId") Long quizId, @Param("questionId") Long questionId);

	@Query(nativeQuery = true, value = "select sum(in_correctcount) from question_analytics_data qad, quiz q "
			+ "where question_id=:questionId and q.status in ('ACTIVE','CLOSE') and q.id = qad.quiz_id and q.id = :quizId")
	int countInCorrectQuestionsAnswers(@Param("quizId") Long quizId, @Param("questionId") Long questionId);

	@Query(nativeQuery = true, value = "select sum(inreview_count) from question_analytics_data qad, quiz q "
			+ "where question_id=:questionId and q.status in ('ACTIVE','CLOSE') and q.id = qad.quiz_id and q.id = :quizId")
	int countInReviewQuestionsAnswers(@Param("quizId") Long quizId, @Param("questionId") Long questionId);

}
