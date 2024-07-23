/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
public class AnswerOptionDTO {
	@NotNull
	private Object option;
	@NotNull
	@Size(min = 1)
	private String type;

}
