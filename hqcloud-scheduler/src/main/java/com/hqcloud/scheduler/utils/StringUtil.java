/**
 * Copyrigt (2021, ) Institute of Software, Chinese Academy of Sciences
 */
package com.hqcloud.scheduler.utils;

import java.util.Random;

/**
 * @author wuheng@otcaix.iscas.ac.cn
 *
 */
public class StringUtil {

	public static String getRandomString() {
		String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 6; i++) {
			int number = random.nextInt(62);
			sb.append(str.charAt(number));
		}
		return sb.toString().toLowerCase();
	}
	
	public static long stringToLong(String value) {
		long weight = 1;
		if (value.endsWith("Ki")) {
			value = value.substring(0, value.length() - 2);
			weight = 1;
		} else if (value.endsWith("Mi")) {
			value = value.substring(0, value.length() - 2);
			weight = 1024;
		} else if (value.endsWith("Gi")) {
			value = value.substring(0, value.length() - 2);
			weight = 1024 * 1024;
		} else if (value.endsWith("Ti")) {
			value = value.substring(0, value.length() - 2);
			weight = 1024 * 1024 * 1024;
		}

		return Long.parseLong(value) * weight;
	}
}
