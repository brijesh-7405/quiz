/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.workruit.quiz.persistence.entity.PlanType;
import com.workruit.quiz.persistence.entity.SubscriptionType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Santosh Bhima
 *
 */
@Getter
@Setter
@ToString
public class SubscriptionPlanDTO {
	@Null
	private Long subscriptionId;
	@NotNull
	@Size(min = 0)
	private String subscriptionName;
	private PlanType planType;
	private SubscriptionType subscriptionType;
	private double totalCost;
	private double fTotalCost;
}
