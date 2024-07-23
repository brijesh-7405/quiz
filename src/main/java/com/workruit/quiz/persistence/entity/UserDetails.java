/**
 * 
 */
package com.workruit.quiz.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Table(name = "user_details")
@Entity
@Data
public class UserDetails extends BaseEntity {
	@Id
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "summary")
	private String summary;
	@Column(name = "description")
	private String description;
	@Column(name = "college_name")
	private String collegeName;
	@Column(name = "current_company_name")
	private String currentCompanyName;
}
