package org.one.stone.soup.core.javascript;

public class JavascriptHelper {

	public static String escape(String data) {
		data = data
				.replaceAll("\\\\", "\\\\\\\\")
				.replaceAll("\"", "\\\\\"")
				.replaceAll("'", "\\\\'");
		return data;
	}
}
