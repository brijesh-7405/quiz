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
public class UserSignupProfileDTO {
	private long id;
	@NotNull(message = "First name cannot be empty/null")
	@Size(min = 2, max = 30, message = "First name size should be minimum of length 1.")
	private String firstName;
	@NotNull(message = "Last name cannot be empty/null")
	@Size(min = 1, max = 30, message = "Last name size should be minimum of length 1.")
	private String lastName;
	@Size(min = 8)
	@Email(message = "Not a proper email format")
	private String primaryEmail;
	@Email
	private String secondaryEmail;
	@Size(min = 10, max = 10, message = "Mobile number should be maximum length of 10")
	private String mobile;
	@NotNull(message = "LoginType cannot be null")
	private String loginType;
	@Size(min = 8, message = "Minimum password length is 8")
	@NotNull(message = "Password and Confirm Password are mandatory")
	private String password;
	@NotNull(message = "Password and Confirm Password are mandatory")
	@Size(min = 8)
	private String confirmPassword;
	private String profileImageUrl;
	@NotNull(message = "User role cannot be empty")
	private UserRole userRole;

}
