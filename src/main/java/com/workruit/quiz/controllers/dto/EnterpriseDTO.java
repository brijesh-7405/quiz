/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import lombok.Data;

/**
 * @author Santosh
 *
 */

@Data
public class EnterpriseDTO {
	@Null
	private Long id;
	@NotNull
	private String name;
	private String logo;
	private String about;
	private String location;
	private String website;
	private String contactPersonName;
	private String contactEmail;
	private String contactPhone;
	private String enterpriseType;
	private String enterpriseCode;
	private String logoKey;
}
