package com.workruit.quiz.controllers.dto;

import java.util.List;

import lombok.Data;

@Data
public class AnswerDTO {
	private List<AnswerOptionDTO> options;
}
