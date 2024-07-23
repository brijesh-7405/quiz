/**
 * 
 */
package com.workruit.quiz.services;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workruit.quiz.controllers.dto.CustomSubscriptionPlanDTO;
import com.workruit.quiz.controllers.dto.SubscriptionDetailsDTO;
import com.workruit.quiz.controllers.dto.SubscriptionPlanDTO;
import com.workruit.quiz.persistence.entity.PlanType;
import com.workruit.quiz.persistence.entity.SubscriptionPlan;
import com.workruit.quiz.persistence.entity.repository.SubscriptionPlanRepository;
import com.workruit.quiz.services.utils.GSTUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Santosh Bhima
 *
 */
@Service
@Slf4j
public class SubscriptionPlanService {
	private @Autowired SubscriptionFeatureService subscriptionFeatureService;
	private @Autowired SubscriptionPlanFeatureMappingService subscriptionPlanFeatureMappingService;
	private @Autowired SubscriptionPlanRepository subscriptionPlanRepository;
	private @Autowired ModelMapper modelMapper;

	private @Autowired RazorPayService razorPayService;

	@Transactional
	public Long addSubscriptionPlan(SubscriptionPlanDTO subscriptionPlanDTO) throws Exception {
		try {
			log.debug("Creating subscription with details:{}", subscriptionPlanDTO);
			SubscriptionPlan subscriptionPlan = modelMapper.map(subscriptionPlanDTO, SubscriptionPlan.class);
			subscriptionPlan.setActualCost(GSTUtils.getActualCost(subscriptionPlan.getTotalCost()));
			subscriptionPlan.setCgst(GSTUtils.getCGST(subscriptionPlan.getTotalCost()));
			subscriptionPlan.setSgst(GSTUtils.getSGST(subscriptionPlan.getTotalCost()));
			subscriptionPlan.setFactualCost(GSTUtils.getActualCost(subscriptionPlan.getFtotalCost()));
			subscriptionPlan.setFcgst(GSTUtils.getCGST(subscriptionPlan.getFtotalCost()));
			subscriptionPlan.setFsgst(GSTUtils.getSGST(subscriptionPlan.getFtotalCost()));
			subscriptionPlan.setFtotalCost(subscriptionPlanDTO.getFTotalCost());
			subscriptionPlan.setRazorPayPlanId(razorPayService.createRazorPayPlan(subscriptionPlan));

			subscriptionPlanRepository.save(subscriptionPlan);
			return subscriptionPlan.getSubscriptionId();
		} catch (Exception e) {
			log.error("Error while saving subscription", e);
			throw e;
		}
	}

	@Transactional
	public Long addCustomSubscriptionPlan(CustomSubscriptionPlanDTO customSubscriptionPlanDTO) throws Exception {
		try {

			Long planId = addSubscriptionPlan(customSubscriptionPlanDTO.getSubscription());
			
			customSubscriptionPlanDTO.getFeatures().forEach(feature -> {
				Long featureId = subscriptionFeatureService.addSubscriptionFeature(feature);
				try {
					subscriptionPlanFeatureMappingService.addSubscriptionPlanFeatureMapping(planId, featureId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			return planId;
		} catch (Exception e) {
			log.error("Error while saving subscription", e);
			throw e;
		}
	}

	public List<SubscriptionDetailsDTO> listSubscriptions(int page, int size, String subscriptionName,
			PlanType planType) {
		try {
			log.debug("Retrieving the subscriptions");
			if (StringUtils.isBlank(subscriptionName) && planType == null) {
				List<SubscriptionPlan> subscriptionPlans = subscriptionPlanRepository
						.findAll(PageRequest.of(page, size)).getContent();
				return subscriptionPlans.stream().map(subscriptionPlan -> {
					try {
						return subscriptionPlanFeatureMappingService.getSubscriptionFeaturesBySubscriptionPlan(
								modelMapper.map(subscriptionPlan, SubscriptionPlanDTO.class).getSubscriptionId());
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}).collect(Collectors.toList());
			} else {
				List<SubscriptionPlan> subscriptionPlans = subscriptionPlanRepository
						.findAllBySubscriptionNameAndPlanType(subscriptionName, planType, PageRequest.of(page, size));
				return subscriptionPlans.stream().map(subscriptionPlan -> {
					try {
						return subscriptionPlanFeatureMappingService.getSubscriptionFeaturesBySubscriptionPlan(
								modelMapper.map(subscriptionPlan, SubscriptionPlanDTO.class).getSubscriptionId());
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}).collect(Collectors.toList());
			}
		} catch (Exception e) {
			log.error("Error while getting subscriptions", e);
			throw e;
		}
	}

	public long getSubscriptionsCount(String subscriptionName, PlanType planType) {
		if (StringUtils.isBlank(subscriptionName) && planType == null) {
			return subscriptionPlanRepository.count();
		} else {
			return subscriptionPlanRepository.countBySubscriptionNameAndPlanType(subscriptionName, planType);
		}
	}
}
