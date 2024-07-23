/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.QuizTopicList;

/**
 * @author Santosh
 *
 */
public interface QuizTopicListRepository extends JpaRepository<QuizTopicList, Long> {
	/**
	 * Delete the quiz category list by id.
	 * 
	 * @param quizId
	 */
	void deleteByQuizId(Long quizId);

	List<QuizTopicList> findByQuizId(Long quizId);

	Long countByQuizIdInAndTopicIdIn(List<Long> quizId, List<Long> topicIds);

	Long countByQuizIdInAndTopicId(List<Long> quizId, Long topicId);

	List<QuizTopicList> findByQuizIdInAndTopicIdIn(List<Long> quizId, List<Long> topicIds);

	List<QuizTopicList> findByQuizIdIn(List<Long> quizId);
}
