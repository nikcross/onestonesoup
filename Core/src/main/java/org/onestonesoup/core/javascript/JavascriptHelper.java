package org.onestonesoup.core.javascript;

public class JavascriptHelper {

	public static String escape(String data) {
		data = data
				.replaceAll("\\\\", "\\\\\\\\")
				.replaceAll("\"", "\\\\\"")
				.replaceAll("'", "\\\\'");
		return data;
	}
}
