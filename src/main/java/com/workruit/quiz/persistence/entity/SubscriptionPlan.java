/**
 * 
 */
package com.workruit.quiz.persistence.entity;

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
 * @author Dell
 *
 */
@Table(name = "subscription_plans")
@Entity
@Getter
@Setter
public class SubscriptionPlan extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@Column(name = "subscription_id")
	private Long subscriptionId;
	@Column(name = "subscription_name")
	private String subscriptionName;
	@Column(name = "plan_type")
	@Enumerated(value = EnumType.STRING)
	private PlanType planType;
	@Column(name = "subscription_type")
	@Enumerated(value = EnumType.STRING)
	private SubscriptionType subscriptionType;
	@Column(name = "total_cost")
	private Double totalCost;
	@Column(name = "sgst")
	private Double sgst;
	@Column(name = "cgst")
	private Double cgst;
	@Column(name = "actual_cost")
	private Double actualCost;
	@Column(name = "ftotal_cost")
	private Double ftotalCost;
	@Column(name = "fsgst")
	private Double fsgst;
	@Column(name = "fcgst")
	private Double fcgst;
	@Column(name = "factual_cost")
	private Double factualCost;
	@Column(name = "razorpay_planid")
	private String razorPayPlanId;
}
