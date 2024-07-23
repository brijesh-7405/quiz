/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.workruit.quiz.persistence.entity.User.UserRole;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Santosh
 *
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class UserSocialSignupProfileDTO {
	private long id;
	@NotNull(message = "First name cannot be empty/null")
	@Size(min = 1, message = "First name size should be minimum of length 1.")
	private String firstName;
	@NotNull(message = "Last name cannot be empty/null")
	@Size(min = 1, message = "Last name size should be minimum of length 1.")
	private String lastName;
	@Size(min = 8)
	@Email(message = "Not a proper email format")
	private String primaryEmail;
	@Email
	private String secondaryEmail;
	@NotNull(message = "LoginType cannot be null")
	private String loginType;
	@Size(min = 8, message = "Minimum password length is 8")
	private String profileImageUrl;
	@NotNull(message = "User role cannot be empty")
	private UserRole userRole;

}
