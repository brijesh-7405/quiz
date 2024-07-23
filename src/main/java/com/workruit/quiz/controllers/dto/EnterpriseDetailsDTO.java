/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class EnterpriseDetailsDTO {
	private Long id;
	private String name;
	private Long numberOfTopics;
	private Integer numberOfQuizs;
	private String logo;
	private String logoKey;
	private String enterpriseLocation;
	private String enterprisePersonName;
	private String enterpriseWebsite;
	private String enterpriseContactEmail;
	private String enterpriseContactPhone;
	private String about;
	@JsonInclude(value = Include.NON_NULL)
	private String status;
	@JsonIgnore
	private Timestamp createdDate;
	private String enterpriseCreatedDate;
}
