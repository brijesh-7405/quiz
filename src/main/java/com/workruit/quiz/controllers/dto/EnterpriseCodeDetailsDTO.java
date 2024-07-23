/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Santosh Bhima
 *
 */
@Setter
@Getter
public class EnterpriseCodeDetailsDTO {
	private Long enterpriseId;
	private String enterpriseName;
	private String code;
	private Status status;
	public enum Status {
		APPROVED, REJECTED, PENDING
	}
}
