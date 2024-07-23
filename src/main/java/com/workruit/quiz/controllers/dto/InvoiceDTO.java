/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Santosh Bhima
 *
 */
@Getter
@Setter
public class InvoiceDTO {
	private String invoiceNumber;
	private String enterpriseName;
	private String buyerName;
	private String buyerEmail;

	private double sgst;
	private double cgst;
	private double actualCost;
	private double totalCost;
	
	private Timestamp transacionDate;
	private String orderId;
	private double discount;
}
