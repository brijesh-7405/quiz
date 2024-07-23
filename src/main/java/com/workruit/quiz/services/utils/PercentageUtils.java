package com.workruit.quiz.services.utils;

import java.text.DecimalFormat;

public class PercentageUtils {

	public static int getCorrectPercentageInInt(long correct, long total) {
		return (int) (correct * 100 / total);
	}

	public static float getPercentageInDouble(long value, long total) {
		Double x = value * 100 / (double) total;
		DecimalFormat df = new DecimalFormat("#.00");
		return Float.parseFloat(df.format(x.floatValue()));
	}
}
