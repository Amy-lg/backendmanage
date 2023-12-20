package com.power.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * description md5加密
 * @author cyk
 * @date 2023/12
 */
public class Md5Util {

	public static String encrypt(String dataStr) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(dataStr.getBytes(StandardCharsets.UTF_8));
			byte[] byt = m.digest();
			StringBuilder result = new StringBuilder();
			for (byte b : byt) {
				result.append(Integer.toHexString((0x000000FF & b) | 0xFFFFFF00)
						.substring(6));
			}
			return result.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
