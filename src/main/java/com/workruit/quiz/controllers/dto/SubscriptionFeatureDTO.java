/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import javax.validation.constraints.Null;

import com.workruit.quiz.persistence.entity.Feature;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Santosh Bhima
 *
 */
@Getter
@Setter
@ToString
public class SubscriptionFeatureDTO {
	@Null
	private Long subscriptionFeatureId;
	private Feature featureName;
	private long featureCount;
}
