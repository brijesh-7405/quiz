package com.workruit.quiz.controllers.dto;

import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class UserProfileDTO {
	private long id;
	@Size(min = 2, max = 30, message = "First name size should be minimum of length 1.")
	private String firstName;
	@Size(min = 2, max = 30, message = "Last name size should be minimum of length 1.")
	private String lastName;
	private String primaryEmail;
	private String profileImageUrl;

	private String profileImageUrlKey;
	private String loginType;
}
