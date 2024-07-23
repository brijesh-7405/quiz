/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.Answer;

/**
 * @author Santosh
 *
 */
public interface AnswerRepository extends JpaRepository<Answer, Long> {
	Answer findByQuestionId(Long questionId);
}
