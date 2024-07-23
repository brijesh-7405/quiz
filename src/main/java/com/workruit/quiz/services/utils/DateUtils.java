package com.workruit.quiz.services.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;

public class DateUtils {
	public static String format(Timestamp timestamp) {
		if (timestamp != null) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, yyyy HH:mm");
			return simpleDateFormat.format(new Date(timestamp.getTime()));
		}
		return null;
	}

	public static String format(Date date) {
		if (date != null) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, yyyy HH:mm");
			return simpleDateFormat.format(date);
		}
		return null;
	}

	public static Date resetTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	public static Date next10Min() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, 10);
		return calendar.getTime();
	}

	public static Date nextYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, 1);
		return calendar.getTime();
	}
	
	public static Date getDateinUTC(Date date) {
		TimeZone timeZone = TimeZone.getDefault();
		if (timeZone.getID().equals("UTC")) {
			return date;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int hours = calendar.get(Calendar.HOUR);
		int mins = calendar.get(Calendar.MINUTE);
		if (hours < 5) {
			if (mins < 30) {
				return nextDayStartingTime(date);
			}
		}
		return date;
	}

	public static Date getPreviousStartingTime(Date date) {
		Date newDate = resetTime(date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(newDate);
		calendar.add(Calendar.HOUR, -24);
		return calendar.getTime();
	}

	public static Date nextDayStartingTime(Date date) {
		Date newDate = resetTime(date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(newDate);
		calendar.add(Calendar.HOUR, 24);
		return calendar.getTime();
	}
	
	public static Date nextHourStartingTime(Date date) {
		Date newDate = resetTime(date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(newDate);
		calendar.add(Calendar.HOUR, 1);
		return calendar.getTime();
	}

	public static Date addHours(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.HOUR, 5);
		calendar.add(Calendar.MINUTE, 30);
		return calendar.getTime();
	}

	public static Date incrementDays(Date date, int days) {
		Date newDate = resetTime(date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(newDate);
		calendar.add(Calendar.DAY_OF_MONTH, days);
		return calendar.getTime();
	}

	public static Date getDate(String date) throws ParseException {
		if (date != null) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, yyyy HH:mm");
			return simpleDateFormat.parse(date);
		}
		return null;
	}

	public static Converter<Timestamp, String> getConvertor() {
		return new AbstractConverter<Timestamp, String>() {
			protected String convert(Timestamp source) {
				return DateUtils.format(source);
			}
		};
	}
}
