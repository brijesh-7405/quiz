package com.workruit.quiz.controllers.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message {
	private String description;
	private String title;
}
