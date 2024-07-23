/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class UserQuizAnalyticsStatsDTO {
	public UserQuizAnalyticsStatsDTO() {
	}

	public UserQuizAnalyticsStatsDTO(String grade, Integer startRange, Integer endRange, Long numberOfUsers) {
		super();
		this.grade = grade;
		this.startRange = startRange;
		this.endRange = endRange;
		this.numberOfUsers = numberOfUsers;
	}

	private String grade;
	private Integer startRange;
	private Integer endRange;
	private Long numberOfUsers;
}
