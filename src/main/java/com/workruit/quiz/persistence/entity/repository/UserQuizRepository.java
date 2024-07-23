/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.workruit.quiz.persistence.entity.UserQuiz;
import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizResult;
import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizStatus;

/**
 * @author Santosh
 *
 */
public interface UserQuizRepository extends JpaRepository<UserQuiz, Long> {
	UserQuiz findByIdAndUserId(Long id, Long userId);

	UserQuiz findByIdAndUserIdAndStatus(Long id, Long userId, UserQuizStatus status);

	@Query(value = "select quiz_id,count(*) from user_quiz group by quiz_id order by count(*) desc limit 5", nativeQuery = true)
	List groupByQuizId();

	@Query(value = "select quiz_status,count(*) from user_quiz where quiz_id=? group by quiz_status order by count(*) desc", nativeQuery = true)
	List groupByQuizIdAndQuizStatus(Long quizId);

	@Query(value = "select quiz_result,count(*) from user_quiz where quiz_id=? and quiz_status=2 group by quiz_result order by count(*) desc", nativeQuery = true)
	List groupByQuizResult(Long quizId);

	List<UserQuiz> findByUserId(Long userId, Pageable pageRequest);

	Long countByUserId(Long userId);

	UserQuiz findByUserIdAndQuizId(Long userId, Long quizId);
	
	List<UserQuiz> findByUserIdAndQuizIdIn(Long userId, List<Long> quizIds);

	Long countByQuizId(Long quizId);

	List<UserQuiz> findByResultIn(List<UserQuizResult> results);

	List<UserQuiz> findByQuizIdAndStatusIn(Long quizId, List<UserQuizStatus> status);

	List<UserQuiz> findByStatusIn(List<UserQuizStatus> status);

	List<UserQuiz> findByQuizId(Long quizId);
	
	Long countByQuizIdAndQuizTimedOutAndStatus(Long quizId, boolean timedout,UserQuizStatus status);
}
