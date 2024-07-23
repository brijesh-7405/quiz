/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import com.workruit.quiz.persistence.entity.UserEnterpriseRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author Santosh Bhima
 *
 */
public interface UserEnterpriseRequestRepository extends CrudRepository<UserEnterpriseRequest, Long> {
	List<UserEnterpriseRequest> findByEnterpriseId(Long enterpriseId);
	List<UserEnterpriseRequest> findByUserId(Long userId);

	List<UserEnterpriseRequest> findByEnterpriseId(Long enterpriseId, Pageable newPAgeRequest);

	Long countByEnterpriseId(long id);

	UserEnterpriseRequest findByEnterpriseIdAndUserId(Long enterpriseId, Long userId);
}
