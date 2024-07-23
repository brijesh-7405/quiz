/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.workruit.quiz.persistence.entity.Enterprise;
import com.workruit.quiz.persistence.entity.Enterprise.EnterpriseType;

/**
 * @author Santosh
 *
 */
public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {
	Enterprise findByContactEmail(String email);

	Enterprise findByEnterpriseCodeAndEnterpriseType(String code, EnterpriseType enterpriseType);

	@Query(value = "select distinct e.id, e.name,e.location,e.contact_person_name,e.contact_email,e.about,e.creation_date,e.logo,e.contact_phone,e.website from enterprise e "
			+ "inner join quiz q on e.id=q.enterprise_id where q.status='ACTIVE' and ( enterprise_type='PUBLIC') and q.id not in (select quiz_id from user_quiz uq where user_id=?1) limit ?2,?3 ", nativeQuery = true)
	List<Object[]> getEnterprisesWithActiveQuizs(Long userId, int startOffset, int endOffset);

	@Query(value = "select count(distinct e.id) from enterprise e inner join quiz q on e.id=q.enterprise_id where "
			+ "q.status='ACTIVE' and ( enterprise_type='PUBLIC') and q.id not in (select quiz_id from user_quiz uq where user_id=?1)", nativeQuery = true)
	int getEnterprisesWithActiveQuizsCount(Long userId);
	
	@Query(value = "select distinct e.id, e.name,e.location,e.contact_person_name,e.contact_email,e.about,e.creation_date,e.logo,e.contact_phone,e.website from enterprise e inner join quiz q"
			+ " on e.id=q.enterprise_id where q.status='ACTIVE' and ( enterprise_type='PUBLIC' or enterprise_id in ( ?4 ) ) and q.id not in (select quiz_id from user_quiz uq where uq.user_id=?1)"
			+ " limit ?2,?3 ", nativeQuery = true)
	List<Object[]> getEnterprisesWithActiveQuizsPublicPrivate(Long userId, int startOffset, int endOffset, List<Long> enterpriseId);

	@Query(value = "select count(distinct e.id) from enterprise e inner join quiz q on e.id=q.enterprise_id where "
			+ "q.status='ACTIVE'  and ( enterprise_type='PUBLIC' or enterprise_id in (?1)) and q.id not in (select quiz_id from user_quiz uq where uq.user_id=?2) ", nativeQuery = true)
	int getEnterprisesWithActiveQuizsCountPublicPrivate(List<Long> enterpriseId, Long userId);

	Enterprise findByIdAndEnterpriseType(Long enterpriseId, EnterpriseType enterpriseType);

}
