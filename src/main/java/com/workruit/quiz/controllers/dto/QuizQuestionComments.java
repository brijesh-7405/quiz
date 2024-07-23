package com.workruit.quiz.controllers.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizQuestionComments {
	private List<QuestionCommentDTO> questions;
	private String comment;
}
