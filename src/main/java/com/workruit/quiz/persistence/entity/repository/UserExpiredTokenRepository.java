/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.UserExpiredToken;

/**
 * @author Santosh
 *
 */
public interface UserExpiredTokenRepository extends JpaRepository<UserExpiredToken, Long> {
	Long countByUserIdAndAccessToken(Long userId, String accessToken);
}
