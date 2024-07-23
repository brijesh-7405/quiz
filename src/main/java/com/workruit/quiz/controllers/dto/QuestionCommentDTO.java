package com.workruit.quiz.controllers.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionCommentDTO {
	private Long questionId;
	private String comment;
}
