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
public class PurchaseOrderResponse {
	private String purchaseOrderId;
	private String status;
	private String transactionId;
	private String razorSubscriptionId;
}
