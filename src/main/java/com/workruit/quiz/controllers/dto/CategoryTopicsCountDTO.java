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
public class CategoryTopicsCountDTO {
	public CategoryTopicsCountDTO(String category, long count) {
		this.category = category;
		this.count = count;
	}

	public CategoryTopicsCountDTO() {
	}

	private String category;
	private long count;
}
