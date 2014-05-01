package org.one.stone.soup.core;

import java.util.ArrayList;
import java.util.List;

public class ExceptionHelper {

	public static String[] getTrace(Throwable throwable) {
		StackTraceElement[] parts = throwable.getStackTrace();
		List<String> list = new ArrayList<String>();
		list.add(throwable.getMessage());
		for(StackTraceElement part: parts) {
			list.add(part.toString());
		}
		return list.toArray(new String[]{});
	}
}
