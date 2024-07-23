package com.workruit.quiz.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Table(name = "user_verification")
@Entity
@Getter
@Setter
public class UserVerification {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "auto")
	private Long id;
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "otp_code")
	private String otp;
}
