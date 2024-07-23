/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import com.workruit.quiz.persistence.entity.UserQuizAnswers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author Santosh
 *
 */
public interface UserQuizAnswersRepository extends JpaRepository<UserQuizAnswers, Long> {
	List<UserQuizAnswers> findByUserQuizIdAndUserId(Long userQuizId, Long userId);

	Long countByUserQuizIdAndUserId(Long userQuizId, Long userId);

	UserQuizAnswers findByUserQuizIdAndUserIdAndQuestionId(Long userQuizId, Long userId, Long questionId);

	@Query(value = "select count(ua.id) from user_answers ua inner join questions q on ua.question_id = q.id where "
			+ "ua.user_quiz_id=? and ua.user_id =? and q.question_type != ('LIKE_OR_DISLIKE') and ua.answer is not null and ua.answer!='' ", nativeQuery = true)
	Long countByUserQuizIdAndUserIdWithoutLikeDislike(Long userQuizId, Long userId);
}
