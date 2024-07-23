/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Santosh Bhima
 *
 */
@Getter
@Setter
public class EnterpriseSubscriptionDetailsDTO {
	private SubscriptionDetailsDTO subscriptionDetails;
	private Long quizLimitUsage;
	private Date subscriptionEndDate;
	private Date subscriptionStartDate;
}
