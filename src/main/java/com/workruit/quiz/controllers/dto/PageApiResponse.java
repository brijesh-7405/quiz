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
public class PageApiResponse extends ApiResponse {
	private int size;
	private int page;
	private long numberOfRecords;
	private String status;
}
