package org.onestonesoup.core.javascript;

public class JavascriptHelper {

	public static String escape(String data) {
		data = data
				.replaceAll("\r", "\\r")
				.replaceAll("\n", "\\n")
				.replaceAll("\t", "\\\\\t")
				.replaceAll("\\\\", "\\\\\\\\")
				.replaceAll("\"", "\\\\\"")
				.replaceAll("'", "\\\\'");
		return data;
	}
}
