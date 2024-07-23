package com.workruit.quiz.controllers.dto;

import lombok.Data;

@Data
public class PasswordDTO {
	private String newPassword;
	private String confirmPassword;
}
