package com.workruit.quiz.controllers.dto;

import lombok.Data;

@Data
public class QuestionAnswerDTO {
	private Long quizId;
	private QuestionDTO question;
	private AnswerDTO answer;
}
