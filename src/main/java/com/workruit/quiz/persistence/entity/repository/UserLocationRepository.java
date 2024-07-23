/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.UserLocation;

/**
 * @author Santosh
 *
 */
public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {
	UserLocation findByUserId(Long userId);
}
