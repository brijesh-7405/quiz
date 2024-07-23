package com.workruit.quiz.controllers.dto;

import java.util.List;

import lombok.Data;

@Data
public class CategoryListDTO {
	private Long id;
	private String name;
	private List<CategoryDTO> topics;
}
