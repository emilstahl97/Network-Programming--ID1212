package com.kth.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApplicationUtils {

	private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");

	public static String getTime() {
		return dateTimeFormatter.format(LocalDateTime.now());
	}

}
