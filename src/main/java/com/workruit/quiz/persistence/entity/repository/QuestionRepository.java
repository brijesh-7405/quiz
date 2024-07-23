/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import com.workruit.quiz.persistence.entity.Question;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Santosh
 *
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {
	List<Question> findByQuizId(Long quizId);

	Long countByQuizId(Long quizId);

	List<Question> findByQuizId(Long quizId, Pageable page);

	Question findByIdAndQuizId(Long id, Long quizId);

	Long countByQuizIdAndQuestionTypeNot(Long id, Question.QuestionType questionType);
}
