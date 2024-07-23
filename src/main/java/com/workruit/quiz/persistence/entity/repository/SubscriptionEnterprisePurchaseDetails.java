/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.workruit.quiz.persistence.entity.BaseEntity;
import com.workruit.quiz.persistence.entity.SubscriptionEnterprisePurchase;
import com.workruit.quiz.persistence.entity.SubscriptionPlanFeatureMapping;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Santosh Bhima
 *
 */
@Table(name = "subscription_enterprise_purchase_details")
@Getter
@Setter
@Entity
public class SubscriptionEnterprisePurchaseDetails extends BaseEntity {
	@Id
	@GeneratedValue(generator = "native", strategy = GenerationType.AUTO)
	@Column(name = "subscription_purchase_details_id")
	private Long purchaseDetailsId;
	@OneToOne
	@JoinColumn(name = "mapping_id", referencedColumnName = "mapping_id")
	private SubscriptionPlanFeatureMapping mapping;
	@OneToOne
	@JoinColumn(name = "subscription_purchase_id", referencedColumnName = "subscription_purchase_id")
	private SubscriptionEnterprisePurchase purchase;
}
