/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.UserTopics;

/**
 * @author Santosh
 *
 */
public interface UserTopicsRepository extends JpaRepository<UserTopics, Long> {
	List<UserTopics> findByUserId(Long userId);

	void deleteByUserId(Long userId);
}
