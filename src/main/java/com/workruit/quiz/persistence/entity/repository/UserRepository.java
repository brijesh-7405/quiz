/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.User;
import com.workruit.quiz.persistence.entity.User.UserRole;

/**
 * @author Santosh
 *
 */
public interface UserRepository extends JpaRepository<User, Long> {
	User findByMobile(String mobile);

	User findByPrimaryEmail(String email);

	User findByPrimaryEmailAndPassword(String email, String password);

	List<User> findByUserRole(UserRole userRole);

	Long countByUserRoleIn(List<UserRole> roles);

	List<User> findByUserRole(UserRole userRole, Pageable page);
}
