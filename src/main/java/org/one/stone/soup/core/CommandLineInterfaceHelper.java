package org.one.stone.soup.core;

import java.util.HashMap;
import java.util.Map;

public class CommandLineInterfaceHelper {

	public static final Map<String,String> getParameters(String commandLine) {
		return getParameters(commandLine.split(" "));
	}
	
	public static final Map<String,String> getParameters(String[] parts) {
		Map<String,String> parameters = new HashMap<String,String>();
		
		for (int i=0;i<parts.length;i++) {
			if (parts[i].startsWith("-")) {
				String key = parts[i].substring(1);
				i++;
				String value = parts[i];
				parameters.put(key, value);
			}
		}
		
		return parameters;
	}
	
	
}
