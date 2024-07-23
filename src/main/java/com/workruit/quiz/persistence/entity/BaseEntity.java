/**
 * 
 */
package com.workruit.quiz.persistence.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
@Embeddable
@MappedSuperclass
public class BaseEntity {
	@Column(name = "creation_date", insertable = false, updatable = false)
	private Timestamp createdDate;
	@Column(name = "modified_date", insertable = false, updatable = false)
	private Timestamp modifiedDate;
}
