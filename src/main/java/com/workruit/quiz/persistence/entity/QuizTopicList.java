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
@Table(name = "quiz_topic_list")
public class QuizTopicList {
	public QuizTopicList() {
		super();
	}

	public QuizTopicList(long topicId, long quizId) {
		super();
		this.topicId = topicId;
		this.quizId = quizId;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private long id;
	@Column(name = "topic_id")
	private long topicId;
	@Column(name = "quiz_id")
	private long quizId;

}