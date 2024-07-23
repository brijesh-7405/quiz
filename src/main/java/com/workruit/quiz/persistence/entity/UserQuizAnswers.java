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
@Table(name = "user_answers")
@Entity
@Data
public class UserQuizAnswers extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private Long id;
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "user_quiz_id")
	private Long userQuizId;
	@Column(name = "question_id")
	private Long questionId;
	@Column(name = "answer")
	private String option;

}
