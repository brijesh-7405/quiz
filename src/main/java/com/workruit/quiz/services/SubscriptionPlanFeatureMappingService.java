/**
 * 
 */
package com.workruit.quiz.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.controllers.dto.SubscriptionDetailsDTO;
import com.workruit.quiz.persistence.entity.Feature;
import com.workruit.quiz.persistence.entity.SubscriptionFeature;
import com.workruit.quiz.persistence.entity.SubscriptionPlan;
import com.workruit.quiz.persistence.entity.SubscriptionPlanFeatureMapping;
import com.workruit.quiz.persistence.entity.repository.SubscriptionFeatureRepository;
import com.workruit.quiz.persistence.entity.repository.SubscriptionPlanFeatureMappingRepository;
import com.workruit.quiz.persistence.entity.repository.SubscriptionPlanRepository;

/**
 * @author Santosh Bhima
 *
 */
@Service
public class SubscriptionPlanFeatureMappingService {
	private @Autowired SubscriptionFeatureRepository subscriptionFeatureRepository;
	private @Autowired SubscriptionPlanRepository subscriptionPlanRepository;
	private @Autowired SubscriptionPlanFeatureMappingRepository subscriptionPlanFeatureMappingRepository;

	public Long addSubscriptionPlanFeatureMapping(Long subscriptionPlanId, Long subscriptionFeatureId)
			throws Exception {
		try {
			Optional<SubscriptionPlan> optionalSubscriptionPlan = subscriptionPlanRepository
					.findById(subscriptionPlanId);
			if (!optionalSubscriptionPlan.isPresent()) {
				throw new WorkruitException("Subscription Plan Not Found");
			}
			Optional<SubscriptionFeature> optionalSubscriptionFeature = subscriptionFeatureRepository
					.findById(subscriptionFeatureId);
			if (!optionalSubscriptionFeature.isPresent()) {
				throw new WorkruitException("Subscription Feature Not Found");
			}
			SubscriptionPlanFeatureMapping subscriptionPlanFeatureMapping = new SubscriptionPlanFeatureMapping();
			subscriptionPlanFeatureMapping.setSubscriptionPlan(optionalSubscriptionPlan.get());
			subscriptionPlanFeatureMapping.setSubscriptionFeature(optionalSubscriptionFeature.get());
			subscriptionPlanFeatureMappingRepository.save(subscriptionPlanFeatureMapping);
			return subscriptionPlanFeatureMapping.getMappingId();
		} catch (Exception e) {
			throw e;
		}
	}

	public SubscriptionDetailsDTO getSubscriptionFeaturesBySubscriptionPlan(Long subscriptionPlanId) throws Exception {
		try {
			Optional<SubscriptionPlan> optionalSubscriptionPlan = subscriptionPlanRepository
					.findById(subscriptionPlanId);
			if (!optionalSubscriptionPlan.isPresent()) {
				throw new WorkruitException("Subscription Plan Not Found");
			}
			List<SubscriptionPlanFeatureMapping> subscriptionPlanFeatureMappings = subscriptionPlanFeatureMappingRepository
					.findBySubscriptionPlanSubscriptionId(subscriptionPlanId);
			SubscriptionDetailsDTO subscriptionDetailsDTO = new SubscriptionDetailsDTO();
			subscriptionDetailsDTO.setPlanType(optionalSubscriptionPlan.get().getPlanType());
			subscriptionDetailsDTO.setSubscriptionType(optionalSubscriptionPlan.get().getSubscriptionType());
			subscriptionDetailsDTO
					.setFeatures(subscriptionPlanFeatureMappings.stream().map(subscriptionPlanFeatureMapping -> {
						Map<String, Object> feature = new HashMap<>();
						feature.put("feature",
								subscriptionPlanFeatureMapping.getSubscriptionFeature().getFeatureName().name());
						feature.put("count", subscriptionPlanFeatureMapping.getSubscriptionFeature().getFeatureCount());
						if (Feature.QUIZ == subscriptionPlanFeatureMapping.getSubscriptionFeature().getFeatureName()) {
							feature.put("message",
									subscriptionPlanFeatureMapping.getSubscriptionFeature().getFeatureCount() + " "
											+ subscriptionPlanFeatureMapping.getSubscriptionFeature().getFeatureName()
													.name());
						} else if (Feature.PARTICIPANTS == subscriptionPlanFeatureMapping.getSubscriptionFeature()
								.getFeatureName()
								|| Feature.QUESTIONS == subscriptionPlanFeatureMapping.getSubscriptionFeature()
										.getFeatureName()) {
							String output = subscriptionPlanFeatureMapping.getSubscriptionFeature()
									.getFeatureCount() == 999999L ? "UNLIMITED "
											: subscriptionPlanFeatureMapping.getSubscriptionFeature().getFeatureCount()
													+ " ";
							feature.put("message", output
									+ subscriptionPlanFeatureMapping.getSubscriptionFeature().getFeatureName().name()
									+ " PER QUIZ");
						}
						return feature;
					}).collect(Collectors.toList()));	
			subscriptionDetailsDTO.setSubscriptionPlanId(subscriptionPlanId);
			subscriptionDetailsDTO.setSubscriptionPlanName(optionalSubscriptionPlan.get().getSubscriptionName());
			subscriptionDetailsDTO.setTotalCost(optionalSubscriptionPlan.get().getTotalCost());
			subscriptionDetailsDTO.setActualCost(optionalSubscriptionPlan.get().getActualCost());
			subscriptionDetailsDTO.setSgst(optionalSubscriptionPlan.get().getSgst());
			subscriptionDetailsDTO.setCgst(optionalSubscriptionPlan.get().getCgst());
			subscriptionDetailsDTO.setFactualCost(optionalSubscriptionPlan.get().getFactualCost());
			subscriptionDetailsDTO.setFcgst(optionalSubscriptionPlan.get().getFcgst());
			subscriptionDetailsDTO.setFsgst(optionalSubscriptionPlan.get().getSgst());
			subscriptionDetailsDTO.setFtotalCost(optionalSubscriptionPlan.get().getFtotalCost());
			return subscriptionDetailsDTO;
		} catch (Exception e) {
			throw e;
		}
	}
}
