/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class UserDetailsDTO {
	private long id;
	private String email;
	private String name;
	private String phone;
	private boolean enabled;
	private String userRole;
	private Long enterpriseId;
	private Map<String, Object> attributes = new HashMap<>();
	private boolean detailsPopulated;
	private EnterpriseSubscriptionDetailsDTO enterpriseSubscriptionDetails;
}