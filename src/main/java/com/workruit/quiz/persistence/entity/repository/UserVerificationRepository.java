/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.UserVerification;

/**
 * @author Santosh
 *
 */
public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {
	UserVerification findByUserIdAndOtp(Long userId, String otp);

	UserVerification findByUserId(Long userId);
}
