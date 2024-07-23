/**
 * 
 */
package com.workruit.quiz.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Table(name = "questions")
@Entity
@Data
public class Question extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private Long id;
	@Column(name = "category_id")
	private Long categoryId;
	@Column(name = "question")
	private String question;
	@Column(name = "question_type")
	@Enumerated(EnumType.STRING)
	private QuestionType questionType;
	@Column(name = "options")
	private String options;
	@Column(name = "quiz_id")
	private Long quizId;

	public static enum QuestionType {
		TEXT, DROPDOWN, CHECKBOX, TRUE_OR_FALSE, SHORT_TEXT, MULTIPLE_CHOICE, INTEGER, AUDIO, LIKE_OR_DISLIKE
	}
}
