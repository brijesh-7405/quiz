/**
 * 
 */
package com.workruit.quiz.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workruit.quiz.controllers.dto.SubscriptionFeatureDTO;
import com.workruit.quiz.persistence.entity.SubscriptionFeature;
import com.workruit.quiz.persistence.entity.repository.SubscriptionFeatureRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Santosh Bhima
 *
 */
@Service
@Slf4j
public class SubscriptionFeatureService {
	private @Autowired SubscriptionFeatureRepository subscriptionFeatureRepository;
	private @Autowired ModelMapper modelMapper;

	@Transactional
	public Long addSubscriptionFeature(SubscriptionFeatureDTO subscriptionFeatureDTO) {
		try {
			log.debug("Creating a subscription feature:{}", subscriptionFeatureDTO);
			if(subscriptionFeatureDTO.getFeatureCount() == -1L) {
				subscriptionFeatureDTO.setFeatureCount(999999L);
			}
			SubscriptionFeature subscriptionFeature = subscriptionFeatureRepository
					.save(modelMapper.map(subscriptionFeatureDTO, SubscriptionFeature.class));
			return subscriptionFeature.getSubscriptionFeatureId();
		} catch (Exception e) {
			throw e;
		}
	}
}
