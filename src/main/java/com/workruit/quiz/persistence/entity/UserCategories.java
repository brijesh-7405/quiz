/**
 * 
 */
package com.workruit.quiz.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Table(name = "user_interests_categories")
@Entity
@Data
public class UserCategories extends BaseEntity {

	public UserCategories(Long userId, Long categoryId) {
		super();
		this.userId = userId;
		this.categoryId = categoryId;
	}

	public UserCategories() {
		super();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "category_id")
	private Long categoryId;

}
