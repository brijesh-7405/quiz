/**
 * 
 */
package com.workruit.quiz.services;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.workruit.quiz.persistence.entity.PlanType;
import com.workruit.quiz.persistence.entity.SubscriptionEnterprisePurchase;
import com.workruit.quiz.persistence.entity.SubscriptionPlan;
import com.workruit.quiz.persistence.entity.SubscriptionPlanFeatureMapping;
import com.workruit.quiz.persistence.entity.SubscriptionType;
import com.workruit.quiz.persistence.entity.TransactionHistory;
import com.workruit.quiz.persistence.entity.TransactionStatus;
import com.workruit.quiz.persistence.entity.repository.SubscriptionEnterprisePurchaseDetails;
import com.workruit.quiz.persistence.entity.repository.SubscriptionEnterprisePurchaseDetailsRepository;
import com.workruit.quiz.persistence.entity.repository.SubscriptionEnterprisePurchaseRepository;
import com.workruit.quiz.persistence.entity.repository.SubscriptionPlanFeatureMappingRepository;
import com.workruit.quiz.persistence.entity.repository.SubscriptionPlanRepository;
import com.workruit.quiz.persistence.entity.repository.TransactionHistoryRepository;
import com.workruit.quiz.services.utils.DateUtils;

/**
 * @author Santosh Bhima
 *
 */
@Service
public class SubscriptionEnterprisePurchaseService {

	private @Autowired SubscriptionPlanRepository subscriptionPlanRepository;
	private @Autowired SubscriptionPlanFeatureMappingRepository subscriptionPlanFeatureMappingRepository;

	private @Autowired SubscriptionEnterprisePurchaseRepository subscriptionEnterprisePurchaseRepository;
	private @Autowired SubscriptionEnterprisePurchaseDetailsRepository subscriptionEnterprisePurchaseDetailsRepository;

	private @Autowired TransactionHistoryRepository transactionHistoryRepository;

	public boolean isCustomSubscription(Long enterpriseId) {
		SubscriptionEnterprisePurchase subscriptionEnterprisePurchase = subscriptionEnterprisePurchaseRepository
				.findByEnterpriseIdAndSubscriptionStatusAndSubscriptionPlanIdNotNull(enterpriseId, true);
		SubscriptionPlan subscriptionPlan = subscriptionPlanRepository
				.findById(subscriptionEnterprisePurchase.getSubscriptionPlanId()).get();
		if (subscriptionPlan.getSubscriptionType() == SubscriptionType.PRIVATE) {
			return true;
		} else {
			return false;
		}
	}

	public void assignSubscriptionForEnterprise(Long enterpriseId, Long subscriptionPlanId, long userId) {
		SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findById(subscriptionPlanId).get();
		try {
			List<SubscriptionPlanFeatureMapping> subscriptionPlanFeatureMappings = subscriptionPlanFeatureMappingRepository
					.findBySubscriptionPlanSubscriptionId(subscriptionPlanId);
			SubscriptionEnterprisePurchase subscriptionEnterprisePurchase = new SubscriptionEnterprisePurchase();
			subscriptionEnterprisePurchase.setEnterpriseId(enterpriseId);
			subscriptionEnterprisePurchase.setSubscriptionPurchaseDate(new Timestamp(System.currentTimeMillis()));
			TransactionHistory transactionHistory = new TransactionHistory();
			transactionHistory.setEnterpriseId(enterpriseId);
			transactionHistory.setSubscriptionPlanId(subscriptionPlanId);
			transactionHistory.setTransactionStatus(TransactionStatus.INITIATED);
			transactionHistory.setTransactionDate(new Timestamp(new Date().getTime()));
			transactionHistory.setUserId(userId);
			transactionHistoryRepository.save(transactionHistory);
			if (subscriptionPlan.getPlanType() == PlanType.CUSTOM
					&& subscriptionPlan.getSubscriptionName().contains("Trial")) {
				subscriptionEnterprisePurchase
						.setSubscriptionEndDate(new Timestamp(DateUtils.resetTime(DateUtils.incrementDays(new Date(), 14)).getTime()));
				subscriptionEnterprisePurchase.setAutoRenew(false);
				subscriptionEnterprisePurchase.setSubscriptionPlanId(subscriptionPlanId);
			} else {
				if (subscriptionPlan.getPlanType() == PlanType.MONTHLY) {
					subscriptionEnterprisePurchase
							.setSubscriptionEndDate(new Timestamp(DateUtils.incrementDays(new Date(), 30).getTime()));
					subscriptionEnterprisePurchase.setAutoRenew(false);
				} else if (subscriptionPlan.getPlanType() == PlanType.WEEKLY) {
					subscriptionEnterprisePurchase
							.setSubscriptionEndDate(new Timestamp(DateUtils.incrementDays(new Date(), 7).getTime()));
					subscriptionEnterprisePurchase.setAutoRenew(false);
				} else if (subscriptionPlan.getPlanType() == PlanType.YEARLY) {
					subscriptionEnterprisePurchase
							.setSubscriptionEndDate(new Timestamp(DateUtils.incrementDays(new Date(), 365).getTime()));
					subscriptionEnterprisePurchase.setAutoRenew(false);
				}
			}
			subscriptionEnterprisePurchase.setSubscriptionStatus(true);
			subscriptionEnterprisePurchaseRepository.save(subscriptionEnterprisePurchase);
			List<SubscriptionEnterprisePurchaseDetails> subscriptionEnterprisePurchaseDetailsList = subscriptionPlanFeatureMappings
					.stream().map(mapping -> {
						SubscriptionEnterprisePurchaseDetails subscriptionEnterprisePurchaseDetails = new SubscriptionEnterprisePurchaseDetails();
						subscriptionEnterprisePurchaseDetails.setPurchase(subscriptionEnterprisePurchase);
						subscriptionEnterprisePurchaseDetails.setMapping(mapping);
						return subscriptionEnterprisePurchaseDetails;
					}).collect(Collectors.toList());
			if (subscriptionPlan.getPlanType() == PlanType.CUSTOM
					&& subscriptionPlan.getSubscriptionName().contains("Trial")) {
				TransactionHistory transactionHistoryComplete = new TransactionHistory();
				transactionHistoryComplete.setEnterpriseId(enterpriseId);
				transactionHistoryComplete.setSubscriptionPlanId(subscriptionPlanId);
				transactionHistoryComplete.setTransactionStatus(TransactionStatus.SUCCESS);
				transactionHistoryComplete.setTransactionDate(new Timestamp(new Date().getTime()));
				transactionHistoryRepository.save(transactionHistoryComplete);
			}
			subscriptionEnterprisePurchaseDetailsRepository.saveAll(subscriptionEnterprisePurchaseDetailsList);
		} catch (Exception e) {
			if (subscriptionPlan.getPlanType() == PlanType.CUSTOM
					&& subscriptionPlan.getSubscriptionName().contains("Trial")) {
				TransactionHistory transactionHistory = new TransactionHistory();
				transactionHistory.setEnterpriseId(enterpriseId);
				transactionHistory.setSubscriptionPlanId(subscriptionPlanId);
				transactionHistory.setTransactionStatus(TransactionStatus.FAILED);
				transactionHistory.setTransactionDate(new Timestamp(new Date().getTime()));
				transactionHistoryRepository.save(transactionHistory);
			}
			throw e;
		}
	}
}
