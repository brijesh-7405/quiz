/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.PlanType;
import com.workruit.quiz.persistence.entity.SubscriptionPlan;

/**
 * @author Dell
 *
 */
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
	SubscriptionPlan findBySubscriptionName(String name);

	Long countBySubscriptionNameAndPlanType(String subscriptionName, PlanType planType);

	List<SubscriptionPlan> findAllBySubscriptionNameAndPlanType(String subscriptionName, PlanType planType, Pageable page);
	
}
