/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.SubscriptionPlanFeatureMapping;

/**
 * @author Santosh Bhima
 *
 */
public interface SubscriptionPlanFeatureMappingRepository extends JpaRepository<SubscriptionPlanFeatureMapping, Long> {
	/**
	 * 
	 * @param subscriptionPlanId
	 * @return
	 */
	List<SubscriptionPlanFeatureMapping> findBySubscriptionPlanSubscriptionId(Long subscriptionPlanId);
}
