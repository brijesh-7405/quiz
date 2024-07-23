/**
 * 
 */
package com.workruit.quiz.services.utils;

import java.text.DecimalFormat;

/**
 * @author Santosh Bhima
 *
 */
public class GSTUtils {
	public static double getActualCost(double totalCost) {
		DecimalFormat df = new DecimalFormat("#.##");
		String dx = df.format(totalCost - getSGST(totalCost) - getCGST(totalCost));
		return Double.valueOf(dx);
	}

	public static double getSGST(double totalCost) {
		DecimalFormat df = new DecimalFormat("#.##");
		String dx = df.format((totalCost * 9) / 100);
		return Double.valueOf(dx);
	}

	public static double getCGST(double totalCost) {
		DecimalFormat df = new DecimalFormat("#.##");
		String dx = df.format((totalCost * 9) / 100);
		return Double.valueOf(dx);
	}
}
