/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.TransactionHistory;
import com.workruit.quiz.persistence.entity.TransactionStatus;

/**
 * @author Santosh Bhima
 *
 */
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

	TransactionHistory findByTransactionIdentifierOrderByTransactionDateDesc(String transactionUUID);

	TransactionHistory findByInvoiceNumberAndEnterpriseId(String invoice, Long enterpriseId);

	List<TransactionHistory> findByEnterpriseIdAndTransactionStatus(Long enterpriseId, TransactionStatus status,
			Pageable page);

	Long countByEnterpriseIdAndTransactionStatus(Long enterpriseId, TransactionStatus status);

	TransactionHistory findByOrderIdAndPaymentId(String orderId, String paymentId);

	List<TransactionHistory> findByOrderIdAndEnterpriseId(String orderId, Long enterpriseId, Pageable page);
}
