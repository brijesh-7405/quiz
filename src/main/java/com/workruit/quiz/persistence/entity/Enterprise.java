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
@Entity
@Table(name = "enterprise")
@Data
public class Enterprise extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private long id;
	@Column(name = "name")
	private String name;
	@Column(name = "about")
	private String about;
	@Column(name = "logo")
	private String logo;
	@Column(name = "location")
	private String location;
	@Column(name = "website")
	private String website;
	@Column(name = "contact_person_name")
	private String contactPersonName;
	@Column(name = "contact_email")
	private String contactEmail;
	@Column(name = "contact_phone")
	private String contactPhone;
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private EnterpriseStatus status = EnterpriseStatus.NOT_VERIFIED;
	@Column(name = "enterprise_code")
	private String enterpriseCode;
	@Column(name = "enterprise_type")
	@Enumerated(EnumType.STRING)
	private EnterpriseType enterpriseType;

	public enum EnterpriseStatus {
		VERIFIED, NOT_VERIFIED;
	}

	public enum EnterpriseType {
		PUBLIC, PRIVATE
	}
}
