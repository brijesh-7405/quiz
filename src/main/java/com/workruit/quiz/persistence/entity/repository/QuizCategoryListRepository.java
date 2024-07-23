/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.QuizCategoryList;

/**
 * @author Santosh
 *
 */
public interface QuizCategoryListRepository extends JpaRepository<QuizCategoryList, Long> {
	/**
	 * Delete the quiz category list by id.
	 * 
	 * @param quizId
	 */
	void deleteByQuizId(Long quizId);

	List<QuizCategoryList> findByQuizId(Long quizId);

	List<QuizCategoryList> findByQuizIdIn(List<Long> ids);
}
