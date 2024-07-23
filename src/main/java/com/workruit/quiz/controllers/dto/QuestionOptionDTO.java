package com.workruit.quiz.controllers.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class QuestionOptionDTO {
	@NotNull
	@Size(min = 1)
	private String type;
	@NotNull
	@Size(min = 1)
	private Object option;
}
