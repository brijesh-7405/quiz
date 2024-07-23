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
public class QuestionObjectDTO {

	public QuestionObjectDTO() {
		super();
	}

	public QuestionObjectDTO(@NotNull @Size(min = 1) String question, String url, String explanation, String explanationImage) {
		super();
		this.question = question;
		this.url = url;
		this.explanation = explanation;
		this.explanationImage = explanationImage;
	}

	@NotNull
	@Size(min = 2)
	private String question;
	private String url;
	private String explanation;
	private String explanationImage;
}
