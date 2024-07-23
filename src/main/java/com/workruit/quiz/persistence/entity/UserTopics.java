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
@Table(name = "user_interests_topics")
@Entity
@Data
public class UserTopics extends BaseEntity {

	public UserTopics() {
		super();
	}

	public UserTopics(Long userId, Long topicId) {
		super();
		this.userId = userId;
		this.topicId = topicId;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "topic_id")
	private Long topicId;
}
