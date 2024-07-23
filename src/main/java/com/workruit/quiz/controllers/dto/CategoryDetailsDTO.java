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
public class CategoryDetailsDTO {
	private String name;
	private Long id;
	private String logoURL;
	private Integer numberOfTopics;
	private Long numberOfQuizs;
	private Long enterpriseId;
	private String enterpriseName;
}
