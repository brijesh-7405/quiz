/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class UserInterestsDTO {
	@NotNull
	@Size(min = 1, max = 300)
	private String aboutMe;
	@NotNull
	private String description;
	@NotNull
	@Size(min = 2, max = 50, message = "City should be of minimum length 2 and maximum length 50")
	private String city;
	@NotNull
	@Size(min = 0, max = 50, message = "State should be of minimum length 2 and maximum length 50")
	private String state;
	@NotNull
	@Size(min = 0, max = 50, message = "Country should be of minimum length 2 and maximum length 50")
	private String country;
	@Size(min = 2, max = 500, message = "Address should be of minimum length 2 and maximum length 500")
	private String address;
	@NotNull(message = "DOB is mandatory")
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private String dob;
	private String collegeName;
	@NotNull
	@Size(min = 0, max = 30, message = "Company Name should be of minimum length 2 and maximum length 30")
	private String currentCompanyName;
	@NotNull
	@Size(min = 1)
	private List<Long> interestedCategories;
	@NotNull
	@Size(min = 1)
	private List<Long> interestedTopics;
}
