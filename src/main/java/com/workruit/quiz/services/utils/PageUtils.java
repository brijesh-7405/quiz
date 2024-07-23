package com.workruit.quiz.services.utils;

import org.springframework.data.domain.Sort.Direction;

public class PageUtils {

	public static String getSortByField(String input) {
		return input.split(":")[0];
	}

	public static Direction getSortOrder(String input) {
		String order = input.split(":")[1];
		if (order.equalsIgnoreCase("ASC")) {
			return Direction.ASC;
		}
		if (order.equals("DESC")) {
			return Direction.DESC;
		}
		throw new RuntimeException("Not a valid input");
	}
}
