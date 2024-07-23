/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.Category;

/**
 * @author Santosh
 *
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
	List<Category> findByParentId(Long parentId);

	long countByParentId(Long parentId);

	long countByParentIdIn(List<Long> categoryIds);

	long countByIdIn(List<Long> categoryIds);

	Category findByName(String name);
}
