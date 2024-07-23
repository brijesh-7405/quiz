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
@Data
@Entity
@Table(name = "quiz_category_list")
public class QuizCategoryList {

	public QuizCategoryList() {
		super();
	}

	public QuizCategoryList(long categoryId, long quizId) {
		super();
		this.categoryId = categoryId;
		this.quizId = quizId;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private long id;
	@Column(name = "category_id")
	private long categoryId;
	@Column(name = "quiz_id")
	private long quizId;

}
