/**
 * 
 */
package com.workruit.quiz.persistence.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.quiz.persistence.entity.UserDetails;

/**
 * @author Santosh
 *
 */
public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {

}
