/**
 * 
 */
package com.workruit.quiz.persistence.entity;

import java.util.Date;

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
@Table(name = "user")
@Entity
@Data
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private Long id;
	@Column(name = "first_name")
	private String firstName;
	@Column(name = "last_name")
	private String lastName;
	@Column(name = "password")
	private String password;
	@Column(name = "dob")
	private Date dob;
	@Column(name = "primary_email")
	private String primaryEmail;
	@Column(name = "secondary_email")
	private String secondaryEmail;
	@Column(name = "mobile")
	private String mobile;
	@Column(name = "login_type")
	private String loginType;
	@Column(name = "profile_image_url")
	private String profileImageUrl;
	@Column(name = "user_role")
	@Enumerated(EnumType.STRING)
	private UserRole userRole;
	@Column(name = "enabled")
	private boolean enabled = false;
	@Column(name = "access_code")
	private String accessCode;

	public enum UserRole {
		DEFAULT_USER, ENTERPRISE_USER, SUPERADMIN
	}
}
