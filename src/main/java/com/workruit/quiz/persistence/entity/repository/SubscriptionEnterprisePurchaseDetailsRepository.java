/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.SubscriptionEnterprisePurchase;

/**
 * @author Santosh Bhima
 *
 */
public interface SubscriptionEnterprisePurchaseDetailsRepository
		extends JpaRepository<SubscriptionEnterprisePurchaseDetails, Long> {
	List<SubscriptionEnterprisePurchaseDetails> findByPurchase(SubscriptionEnterprisePurchase purchase);
}
