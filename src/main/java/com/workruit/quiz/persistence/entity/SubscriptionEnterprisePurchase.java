/**
 * 
 */
package com.workruit.quiz.persistence.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "subscription_enterprise_purchase")
@Entity
@Getter
@Setter
public class SubscriptionEnterprisePurchase extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@Column(name = "subscription_purchase_id")
	private Long subscriptionPurchaseId;
	@Column(name = "enterprise_id")
	private Long enterpriseId;
	@Column(name = "auto_renew")
	private boolean autoRenew;
	@Column(name = "subscription_purchase_date")
	private Timestamp subscriptionPurchaseDate;
	@Column(name = "subscription_end_date")
	private Timestamp subscriptionEndDate;
	@Column(name = "subscription_status")
	private boolean subscriptionStatus;
	@Column(name = "razorpay_subscription_id")
	private String razorPaySubscriptionId;
	@Column(name = "subscription_plan_id")
	private Long subscriptionPlanId;
}
