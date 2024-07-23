/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class ApiResponse {
	private String message;
	private Object data;
	private Message msg;
	private String status;
}
