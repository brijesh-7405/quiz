/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Santosh Bhima
 *
 */
@Getter
@Setter
public class CustomSubscriptionPlanDTO {
	@NotNull
	private SubscriptionPlanDTO subscription;
	@NotNull
	@Size(min = 3, max = 4)
	private List<SubscriptionFeatureDTO> features;

}
