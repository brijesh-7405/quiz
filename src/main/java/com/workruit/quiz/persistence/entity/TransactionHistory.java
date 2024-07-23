/**
 * 
 */
package com.workruit.quiz.persistence.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Santosh Bhima
 *
 */
@Table(name = "transaction_history")
@Entity
@Getter
@Setter
public class TransactionHistory extends BaseEntity {
	@Id
	@Column(name = "transaction_id")
	@GeneratedValue(generator = "native", strategy = GenerationType.AUTO)
	private Long transactionId;
	@Column(name = "enterprise_id")
	private Long enterpriseId;
	@Column(name = "transaction_date")
	private Timestamp transactionDate;
	@Column(name = "transaction_status")
	@Enumerated(value = EnumType.STRING)
	private TransactionStatus transactionStatus;
	@Column(name = "subscription_plan_id")
	private Long subscriptionPlanId;
	@Column(name = "order_id")
	private String orderId;
	@Column(name = "signature")
	private String signature;
	@Column(name = "payment_id")
	private String paymentId;
	@Column(name = "transaction_uuid")
	private String transactionIdentifier;
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "provider_payment_json")
	private String providerPaymentJson;
	@Column(name = "workruit_payment_json")
	private String workruitPaymentJson;
	@Column(name = "invoice_number")
	private String invoiceNumber;
}
