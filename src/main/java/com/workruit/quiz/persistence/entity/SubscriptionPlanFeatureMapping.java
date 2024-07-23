/**
 * 
 */
package com.workruit.quiz.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Dell
 *
 */

@Table(name = "subscription_plan_feature_mapping")
@Entity
@Getter
@Setter
public class SubscriptionPlanFeatureMapping extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@Column(name = "mapping_id")
	private Long mappingId;

	@OneToOne
	@JoinColumn(name = "subscription_id", referencedColumnName = "subscription_id")
	private SubscriptionPlan subscriptionPlan;

	@OneToOne
	@JoinColumn(name = "subscription_feature_id", referencedColumnName = "subscription_feature_id")
	private SubscriptionFeature subscriptionFeature;

}
