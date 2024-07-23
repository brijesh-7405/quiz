/**
 * 
 */
package com.workruit.quiz.services;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.persistence.entity.Feature;
import com.workruit.quiz.persistence.entity.SubscriptionEnterprisePurchase;
import com.workruit.quiz.persistence.entity.repository.QuestionRepository;
import com.workruit.quiz.persistence.entity.repository.QuizRepository;
import com.workruit.quiz.persistence.entity.repository.SubscriptionEnterprisePurchaseDetails;
import com.workruit.quiz.persistence.entity.repository.SubscriptionEnterprisePurchaseDetailsRepository;
import com.workruit.quiz.persistence.entity.repository.SubscriptionEnterprisePurchaseRepository;
import com.workruit.quiz.persistence.entity.repository.UserQuizRepository;
import com.workruit.quiz.services.utils.DateUtils;

/**
 * @author Santosh Bhima
 *
 */
@Service
public class SubscriptionLimitService {
	private @Autowired SubscriptionEnterprisePurchaseRepository subscriptionEnterprisePurchaseRepository;
	private @Autowired SubscriptionEnterprisePurchaseDetailsRepository subscriptionEnterprisePurchaseDetailsRepository;
	private @Autowired QuizRepository quizRepository;
	private @Autowired QuestionRepository questionRepository;
	private @Autowired UserQuizRepository userQuizRepository;

	public boolean checkQuizExpiry(Long enterpriseId, Timestamp quizEndDate) throws WorkruitException {
		try {
			SubscriptionEnterprisePurchase subscriptionEnterprisePurchase = subscriptionEnterprisePurchaseRepository
					.findByEnterpriseIdAndSubscriptionStatusAndSubscriptionPlanIdNotNull(enterpriseId, true);
			if (subscriptionEnterprisePurchase == null) {
				throw new WorkruitException("No Active Subscription");
			}
			if (!subscriptionEnterprisePurchase.isAutoRenew()) {
				if (quizEndDate.getTime() > org.apache.commons.lang3.time.DateUtils
						.truncate(subscriptionEnterprisePurchase.getSubscriptionEndDate(), Calendar.DATE).getTime()) {
					throw new WorkruitException(
							"Enable autorenew or ensure quiz end date is less than subscription end date");
				} else {
					return true;
				}
			} else {
				return true;
			}
		} catch (WorkruitException we) {
			throw we;
		}
	}

	public boolean checkQuizLimit(Long enterpriseId) throws Exception {
		try {
			SubscriptionEnterprisePurchase subscriptionEnterprisePurchase = subscriptionEnterprisePurchaseRepository
					.findByEnterpriseIdAndSubscriptionStatusAndSubscriptionPlanIdNotNull(enterpriseId, true);
			if (subscriptionEnterprisePurchase == null) {
				throw new WorkruitException("Enterprise does not have active subscription");
			}
			Long count = (long) quizRepository.findAllByEnterpriseIdAndCreatedDateBetween(enterpriseId,
					new Date(subscriptionEnterprisePurchase.getSubscriptionPurchaseDate().getTime()),
					new Date(subscriptionEnterprisePurchase.getSubscriptionEndDate().getTime())).size();
			
			List<SubscriptionEnterprisePurchaseDetails> subscriptionEnterprisePurchaseDetailsList = subscriptionEnterprisePurchaseDetailsRepository
					.findByPurchase(subscriptionEnterprisePurchase);
			for (SubscriptionEnterprisePurchaseDetails subscriptionEnterprisePurchaseDetails : subscriptionEnterprisePurchaseDetailsList) {
				if (subscriptionEnterprisePurchaseDetails.getMapping().getSubscriptionFeature()
						.getFeatureName() == Feature.QUIZ) {
					long featureCount = subscriptionEnterprisePurchaseDetails.getMapping().getSubscriptionFeature()
							.getFeatureCount();
					if (count >= featureCount) {
						throw new WorkruitException("Maximum Number of Quiz Limit Reached");
					} else {
						return true;
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return false;
	}

	public boolean checkQuestionsLimit(Long enterpriseId, Long quizId) throws Exception {
		try {
			SubscriptionEnterprisePurchase subscriptionEnterprisePurchase = subscriptionEnterprisePurchaseRepository
					.findByEnterpriseIdAndSubscriptionStatusAndSubscriptionPlanIdNotNull(enterpriseId, true);
			if (subscriptionEnterprisePurchase == null) {
				throw new WorkruitException("Enterprise does not have active subscription");
			}
			Long count = questionRepository.countByQuizId(quizId);
			List<SubscriptionEnterprisePurchaseDetails> subscriptionEnterprisePurchaseDetailsList = subscriptionEnterprisePurchaseDetailsRepository
					.findByPurchase(subscriptionEnterprisePurchase);
			for (SubscriptionEnterprisePurchaseDetails subscriptionEnterprisePurchaseDetails : subscriptionEnterprisePurchaseDetailsList) {
				if (subscriptionEnterprisePurchaseDetails.getMapping().getSubscriptionFeature()
						.getFeatureName() == Feature.QUESTIONS) {
					long featureCount = subscriptionEnterprisePurchaseDetails.getMapping().getSubscriptionFeature()
							.getFeatureCount();
					if (count >= featureCount) {
						throw new WorkruitException("Maximum Number of Questions Limit Reached");
					} else {
						break;
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return false;
	}

	public boolean checkApplicantsLimit(Long enterpriseId, int count) throws Exception {
		try {
			SubscriptionEnterprisePurchase subscriptionEnterprisePurchase = subscriptionEnterprisePurchaseRepository
					.findByEnterpriseIdAndSubscriptionStatusAndSubscriptionPlanIdNotNull(enterpriseId, true);
			if (subscriptionEnterprisePurchase == null) {
				throw new WorkruitException("Enterprise does not have active subscription");
			}
			List<SubscriptionEnterprisePurchaseDetails> subscriptionEnterprisePurchaseDetailsList = subscriptionEnterprisePurchaseDetailsRepository
					.findByPurchase(subscriptionEnterprisePurchase);
			for (SubscriptionEnterprisePurchaseDetails subscriptionEnterprisePurchaseDetails : subscriptionEnterprisePurchaseDetailsList) {
				if (subscriptionEnterprisePurchaseDetails.getMapping().getSubscriptionFeature()
						.getFeatureName() == Feature.PARTICIPANTS) {
					long featureCount = subscriptionEnterprisePurchaseDetails.getMapping().getSubscriptionFeature()
							.getFeatureCount();
					if (count >= featureCount) {
						throw new WorkruitException("Maximum Number of Applicants Limit Reached");
					} else {
						break;
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return false;
	}

	public boolean checkParticipantsLimit(Long enterpriseId, Long quizId) throws Exception {
		try {
			SubscriptionEnterprisePurchase subscriptionEnterprisePurchase = subscriptionEnterprisePurchaseRepository
					.findByEnterpriseIdAndSubscriptionStatusAndSubscriptionPlanIdNotNull(enterpriseId, true);
			if (subscriptionEnterprisePurchase == null) {
				throw new WorkruitException("Enterprise does not have active subscription");
			}
			Long count = userQuizRepository.countByQuizId(quizId);
			List<SubscriptionEnterprisePurchaseDetails> subscriptionEnterprisePurchaseDetailsList = subscriptionEnterprisePurchaseDetailsRepository
					.findByPurchase(subscriptionEnterprisePurchase);
			for (SubscriptionEnterprisePurchaseDetails subscriptionEnterprisePurchaseDetails : subscriptionEnterprisePurchaseDetailsList) {
				if (subscriptionEnterprisePurchaseDetails.getMapping().getSubscriptionFeature()
						.getFeatureName() == Feature.PARTICIPANTS) {
					long featureCount = subscriptionEnterprisePurchaseDetails.getMapping().getSubscriptionFeature()
							.getFeatureCount();
					if (count >= featureCount) {
						return false;
					} else {
						return true;
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return false;
	}
}
