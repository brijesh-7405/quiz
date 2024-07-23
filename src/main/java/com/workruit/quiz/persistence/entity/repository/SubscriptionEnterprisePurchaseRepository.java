/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.SubscriptionEnterprisePurchase;

/**
 * @author Santosh Bhima
 *
 */
public interface SubscriptionEnterprisePurchaseRepository extends JpaRepository<SubscriptionEnterprisePurchase, Long> {
	SubscriptionEnterprisePurchase findByEnterpriseIdAndSubscriptionStatusAndSubscriptionPlanIdNotNull(Long enterpriseId, boolean status);
	
	List<SubscriptionEnterprisePurchase> findBySubscriptionStatusAndSubscriptionEndDateBetween(boolean status, Timestamp start, Timestamp end);
	
	SubscriptionEnterprisePurchase findByRazorPaySubscriptionId(String subId);
}
