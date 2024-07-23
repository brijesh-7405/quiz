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
@Getter
@Setter
public class OrderDetailsDTO {
	private String orderId;
	private String orderStatus;
	private SubscriptionDetailsDTO subscriptionDetails;
}
