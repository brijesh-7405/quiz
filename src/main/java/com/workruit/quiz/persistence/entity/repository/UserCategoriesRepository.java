/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.UserCategories;

/**
 * @author Santosh
 *
 */
public interface UserCategoriesRepository extends JpaRepository<UserCategories, Long> {
	List<UserCategories> findByUserId(Long userId);

	void deleteByUserId(Long userId);
}
