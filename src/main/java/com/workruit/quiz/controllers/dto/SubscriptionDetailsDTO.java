/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.util.List;
import java.util.Map;

import com.workruit.quiz.persistence.entity.PlanType;
import com.workruit.quiz.persistence.entity.SubscriptionType;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Santosh Bhima
 *
 */
@Getter
@Setter
public class SubscriptionDetailsDTO {
	private Long subscriptionPlanId;
	private String subscriptionPlanName;
	private SubscriptionType subscriptionType;
	private PlanType planType;
	private List<Map<String, Object>> features;
	private double totalCost;
	private double actualCost;
	private double sgst;
	private double cgst;
	private double ftotalCost;
	private double fcgst;
	private double fsgst;
	private double factualCost;
	
}
