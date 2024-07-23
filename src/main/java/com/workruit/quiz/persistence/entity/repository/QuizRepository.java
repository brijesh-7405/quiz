/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.workruit.quiz.persistence.entity.Quiz;
import com.workruit.quiz.persistence.entity.Quiz.QuizSubmitStatus;

/**
 * @author Santosh
 *
 */
@Transactional
public interface QuizRepository extends JpaRepository<Quiz, Long> {

	Quiz findByIdAndEnterpriseId(Long quizId, Long enterpriseId);

	List<Quiz> findByEnterpriseIdAndStatus(Long enterpriseId, QuizSubmitStatus status);

	List<Quiz> findByEnterpriseId(Long enterpriseId);

	List<Quiz> findByIdNotInAndStatus(List<Long> quidIds, QuizSubmitStatus status, Pageable page);

	@Query(value = "select q.* from quiz q where q.id not in (:quizIds) and q.status = :status and q.enterprise_id in (select e.id from enterprise e where (enterprise_type in ('PRIVATE') and enterprise_code in (:enterprise_code)) or enterprise_type in ('PUBLIC')) order by q.creation_date limit 10", nativeQuery = true)
	List<Quiz> findByIdNotInAndStatusByEnterpriseCodeLimit(@Param("status") String status,
			@Param("enterprise_code") String enterpriseCode, @Param("quizIds") List<Long> quizIds);

	@Query(value = "select q.* from quiz q where q.id not in (:quizIds) and q.status = :status and q.enterprise_id in (select e.id from enterprise e where enterprise_type in ('PUBLIC')) order by q.creation_date limit 10", nativeQuery = true)
	List<Quiz> findByIdNotInAndStatusByEnterprisePublicLimit(@Param("status") String status,
			@Param("quizIds") List<Long> quizIds);
	
	@Query(value = "select q.* from quiz q where q.id not in (20,21,22,25) and q.status = 'ACTIVE' and q.enterprise_id in (select e.id from enterprise e where enterprise_type in ('PUBLIC')) order by q.creation_date limit 10", nativeQuery = true)
	List<Quiz> test();


	//
	List<Quiz> findByStatus(QuizSubmitStatus status, Pageable page);

	@Query(value = "select q.* from quiz q where q.status = :status and q.enterprise_id in (select e.id from enterprise e where (enterprise_type in ('PRIVATE') and enterprise_code in (:enterprise_code)) or enterprise_type in ('PUBLIC')) order by q.creation_date limit 10", nativeQuery = true)
	List<Quiz> findByStatusByEnterpriseCodeLimit(@Param("status") String status,
			@Param("enterprise_code") String enterpriseCode);

	@Query(value = "select q.* from quiz q where q.status = :status and q.enterprise_id in (select e.id from enterprise e where e.enterprise_type = 'PUBLIC') order by q.creation_date limit 10", nativeQuery = true)
	List<Quiz> findByStatusByEnterpriseTypePublicLimit(@Param("status") String status);

	//
	Long countByStatus(QuizSubmitStatus status);

	List<Quiz> findByIdInAndStatus(List<Long> quidIds, QuizSubmitStatus status);

	Optional<Quiz> findByIdAndStatus(Long id, QuizSubmitStatus status);

	Quiz findByIdAndStatusIn(Long id, List<QuizSubmitStatus> multipleStatus);

	Long countById(Long id);

	//
	List<Quiz> findByStatus(QuizSubmitStatus status);

	@Query(value = "select q.* from quiz q where q.status = :status and q.enterprise_id in (select e.id from enterprise e where (enterprise_type in ('PRIVATE') and enterprise_code in (:enterprise_code)) or enterprise_type in ('PUBLIC')) order by q.creation_date", nativeQuery = true)
	List<Quiz> findByStatusByEnterpriseCode(@Param("status") String status,
			@Param("enterprise_code") String enterpriseCode);

	@Query(value = "select q.* from quiz q where q.status = :status and q.enterprise_id in (select e.id from enterprise e where e.enterprise_type = 'PUBLIC') order by q.creation_date", nativeQuery = true)
	List<Quiz> findByStatusByEnterpriseTypePublic(@Param("status") String status);

	//
	List<Quiz> findAllByExpiryDateGreaterThanEqualAndExpiryDateLessThanEqual(Date fromDate, Date toDate);

	List<Quiz> findAllByEnterpriseIdAndCreatedDateBetween(Long enterpriseId, Date fromDate, Date toDate);

	/**
	 * Filter by Enterprise Id and List of Category Ids
	 * 
	 * @param enterpriseId
	 * @param categoryIds
	 * @param startIndex
	 * @param endIndex
	 * @param userId
	 * @return
	 */
	@Query(value = "SELECT id FROM (SELECT q.id FROM enterprise e, quiz q, quiz_topic_list qt "
			+ "WHERE e.id = q.enterprise_id AND q.id = qt.quiz_id AND qt.topic_id IN (:categoryIds)"
			+ " AND q.status in ('ACTIVE') and e.id = :enterpriseId AND ((e.enterprise_code in (:access_code) AND enterprise_type in ('PRIVATE')) or enterprise_type in ('PUBLIC')) "
			+ " and q.id not in (select uq.quiz_id from user_quiz uq where uq.user_id = :userId and uq.quiz_status!='IN_PROGRESS')) as result"
			+ " limit :startIndex,:endIndex", nativeQuery = true)
	List<Long> filterQuizByEnterpriseAndCategoryIdsWithEnterpriseType(@Param("enterpriseId") Long enterpriseId,
			@Param("categoryIds") List<Long> categoryIds, @Param("startIndex") int startIndex,
			@Param("endIndex") int endIndex, @Param("userId") long userId, @Param("access_code") List<String> accessCode);

	@Query(value = "SELECT count(id) FROM (SELECT q.id FROM enterprise e, quiz q, quiz_topic_list qt "
			+ "WHERE e.id = q.enterprise_id  AND q.id = qt.quiz_id  AND qt.topic_id IN (:categoryIds)"
			+ " AND q.status in ('ACTIVE') and e.id = :enterpriseId AND ((e.enterprise_code in (:access_code) AND enterprise_type in ('PRIVATE')) or enterprise_type in ('PUBLIC')) "
			+ " and q.id not in (select quiz_id from user_quiz uq where user_id = :userId and quiz_status!='IN_PROGRESS')) as result"
			+ "", nativeQuery = true)
	Long countByfilterQuizByEnterpriseAndCategoryIdsByEnterpriseType(@Param("enterpriseId") Long enterpriseId,
			@Param("categoryIds") List<Long> categoryIds, @Param("userId") long userId,
			@Param("access_code") List<String> accessCode);
	
	
	@Query(value = "SELECT id FROM (SELECT q.id FROM enterprise e, quiz q, quiz_topic_list qt "
			+ "WHERE e.id = q.enterprise_id AND q.id = qt.quiz_id AND qt.topic_id IN (:categoryIds)"
			+ " AND q.status in ('ACTIVE') and e.id = :enterpriseId AND enterprise_type in ('PUBLIC') AND "
			+ "q.id not in (select quiz_id from user_quiz uq where user_id = :userId and quiz_status!='IN_PROGRESS')) as result"
			+ " limit :startIndex,:endIndex", nativeQuery = true)
	List<Long> filterQuizByEnterpriseAndCategoryIds(@Param("enterpriseId") Long enterpriseId,
			@Param("categoryIds") List<Long> categoryIds, @Param("startIndex") int startIndex,
			@Param("endIndex") int endIndex, @Param("userId") long userId);

	@Query(value = "SELECT count(id) FROM (SELECT q.id FROM enterprise e, quiz q, quiz_topic_list qt "
			+ "WHERE e.id = q.enterprise_id AND enterprise_type in ('PUBLIC') AND q.id = qt.quiz_id AND qt.topic_id IN (:categoryIds)"
			+ " AND q.status in ('ACTIVE') and e.id = :enterpriseId and "
			+ "q.id not in (select quiz_id from user_quiz uq where user_id = :userId and quiz_status!='IN_PROGRESS')) as result"
			+ "", nativeQuery = true)
	Long countByfilterQuizByEnterpriseAndCategoryIds(@Param("enterpriseId") Long enterpriseId,
			@Param("categoryIds") List<Long> categoryIds, @Param("userId") long userId);



	/**
	 * 
	 * @param enterpriseId
	 * @param categoryIds
	 * @param startIndex
	 * @param endIndex
	 * @param userId
	 * @return
	 */
	@Query(value = "SELECT id FROM (SELECT q.id FROM enterprise e, quiz q, quiz_topic_list qt "
			+ "WHERE e.id = q.enterprise_id AND q.id = qt.quiz_id"
			+ " AND q.status in ('ACTIVE') and e.id = :enterpriseId and "
			+ "q.id not in (select quiz_id from user_quiz uq where user_id = :userId and quiz_status!='IN_PROGRESS')) as result"
			+ " limit :startIndex,:endIndex", nativeQuery = true)
	List<Long> filterQuizByEnterprise(@Param("enterpriseId") Long enterpriseId, @Param("startIndex") int startIndex,
			@Param("endIndex") int endIndex, @Param("userId") long userId);

	@Query(value = "SELECT count(id) FROM (SELECT q.id FROM enterprise e, quiz q, quiz_topic_list qt "
			+ "WHERE e.id = q.enterprise_id AND q.id = qt.quiz_id"
			+ " AND q.status in ('ACTIVE') and e.id = :enterpriseId and "
			+ "q.id not in (select quiz_id from user_quiz uq where user_id = :userId and quiz_status!='IN_PROGRESS')) as result"
			+ "", nativeQuery = true)
	Long countByfilterQuizByEnterprise(@Param("enterpriseId") Long enterpriseId, @Param("userId") long userId);

	/**
	 * 
	 * @param enterpriseId
	 * @param categoryIds
	 * @param startIndex
	 * @param endIndex
	 * @param userId
	 * @return
	 */
	@Query(value = "SELECT id FROM (SELECT q.id FROM enterprise e, quiz q, quiz_topic_list qt "
			+ "WHERE e.id = q.enterprise_id AND e.enterprise_type in ('PUBLIC')  AND q.id = qt.quiz_id AND qt.topic_id IN (:categoryIds)"
			+ " AND q.status in ('ACTIVE') and "
			+ "q.id not in (select quiz_id from user_quiz uq where user_id = :userId and quiz_status!='IN_PROGRESS')) as result"
			+ " limit :startIndex,:endIndex", nativeQuery = true)
	List<Long> filterQuizByCategoryIds(@Param("categoryIds") List<Long> categoryIds,
			@Param("startIndex") int startIndex, @Param("endIndex") int endIndex, @Param("userId") long userId);

	@Query(value = "SELECT id FROM (SELECT q.id FROM enterprise e, quiz q, quiz_topic_list qt "
			+ "WHERE e.id = q.enterprise_id"
			+ " AND ((e.enterprise_code in (:enterprise_code) AND enterprise_type in ('PRIVATE')) or enterprise_type in ('PUBLIC')) AND q.id = qt.quiz_id AND qt.topic_id IN (:categoryIds)"
			+ " AND q.status in ('ACTIVE') and "
			+ "q.id not in (select quiz_id from user_quiz uq where user_id = :userId and quiz_status!='IN_PROGRESS')) as result"
			+ " limit :startIndex,:endIndex", nativeQuery = true)
	List<Long> filterQuizByCategoryIdsByEnterpriseType(@Param("categoryIds") List<Long> categoryIds,
			@Param("startIndex") int startIndex, @Param("endIndex") int endIndex, @Param("userId") long userId,
			@Param("enterprise_code") String accessCode);

	@Query(value = "SELECT count(id) FROM (SELECT q.id FROM enterprise e, quiz q, quiz_topic_list qt "
			+ "WHERE e.id = q.enterprise_id AND e.enterprise_type in ('PUBLIC') AND q.id = qt.quiz_id AND qt.topic_id IN (:categoryIds)"
			+ " AND q.status in ('ACTIVE') and "
			+ "q.id not in (select quiz_id from user_quiz uq where user_id = :userId and quiz_status!='IN_PROGRESS')) as result"
			+ "", nativeQuery = true)
	Long countByfilterQuizByCategoryIds(@Param("categoryIds") List<Long> categoryIds, @Param("userId") long userId);

	@Query(value = "SELECT count(id) FROM (SELECT q.id FROM enterprise e, quiz q, quiz_topic_list qt "
			+ "WHERE e.id = q.enterprise_id AND ((e.enterprise_code in (:enterprise_code) AND enterprise_type in ('PRIVATE')) or enterprise_type in ('PUBLIC')) "
			+ "AND q.id = qt.quiz_id AND qt.topic_id IN (:categoryIds)"
			+ " AND q.status in ('ACTIVE') and "
			+ "q.id not in (select quiz_id from user_quiz uq where user_id = :userId and quiz_status!='IN_PROGRESS')) as result"
			+ "", nativeQuery = true)
	Long countByfilterQuizByCategoryIdsByEnterpriseType(@Param("categoryIds") List<Long> categoryIds,
			@Param("userId") long userId, @Param("enterprise_code") String accessCode);

	/**
	 * 
	 * @param enterpriseId
	 * @param categoryIds
	 * @param startIndex
	 * @param endIndex
	 * @param userId
	 * @return
	 */
	@Query(value = "SELECT id FROM (SELECT q.id FROM enterprise e, quiz q, quiz_topic_list qt "
			+ "WHERE e.id = q.enterprise_id AND e.enterprise_type in ('PUBLIC') AND q.id = qt.quiz_id"
			+ " AND q.status in ('ACTIVE') AND "
			+ "q.id not in (select quiz_id from user_quiz uq where user_id = :userId and quiz_status!='IN_PROGRESS')) as result"
			+ " limit :startIndex,:endIndex", nativeQuery = true)
	List<Long> filterQuiz(@Param("startIndex") int startIndex, @Param("endIndex") int endIndex,
			@Param("userId") long userId);

	@Query(value = "SELECT id FROM (SELECT q.id FROM enterprise e, quiz q, quiz_topic_list qt "
			+ "WHERE e.id = q.enterprise_id AND ((e.enterprise_code in (:enterprise_code) AND enterprise_type in ('PRIVATE')) or enterprise_type in ('PUBLIC'))  AND q.id = qt.quiz_id"
			+ " AND q.status in ('ACTIVE') and "
			+ "q.id not in (select quiz_id from user_quiz uq where user_id = :userId and quiz_status!='IN_PROGRESS')) as result"
			+ " limit :startIndex,:endIndex", nativeQuery = true)
	List<Long> filterQuizWithEnterpriseType(@Param("startIndex") int startIndex, @Param("endIndex") int endIndex,
			@Param("userId") long userId, @Param("enterprise_code") String accessCode);

	@Query(value = "SELECT count(id) FROM (SELECT q.id FROM enterprise e, quiz q, quiz_topic_list qt "
			+ "WHERE e.id = q.enterprise_id AND e.enterprise_type in ('PUBLIC')  AND q.id = qt.quiz_id"
			+ " AND q.status in ('ACTIVE') and "
			+ "q.id not in (select quiz_id from user_quiz uq where user_id = :userId and quiz_status!='IN_PROGRESS')) as result"
			+ "", nativeQuery = true)
	Long countByfilterQuiz(@Param("userId") long userId);

	@Query(value = "SELECT count(id) FROM (SELECT q.id FROM enterprise e, quiz q, quiz_topic_list qt "
			+ "WHERE e.id = q.enterprise_id AND ((e.enterprise_code in (:enterprise_code) AND enterprise_type in ('PRIVATE')) or enterprise_type in ('PUBLIC')) AND q.id = qt.quiz_id"
			+ " AND q.status in ('ACTIVE') and "
			+ "q.id not in (select quiz_id from user_quiz uq where user_id = :userId and quiz_status!='IN_PROGRESS')) as result"
			+ "", nativeQuery = true)
	Long countByfilterQuizWithEnterpriseType(@Param("userId") long userId, @Param("enterprise_code") String accessCode);
}
