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

@Table(name = "subscription_features")
@Entity
@Getter
@Setter
public class SubscriptionFeature extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@Column(name = "subscription_feature_id")
	private Long subscriptionFeatureId;
	@Column(name = "feature_name")
	@Enumerated(value = EnumType.STRING)
	private Feature featureName;
	@Column(name = "feature_count")
	private long featureCount;
}
