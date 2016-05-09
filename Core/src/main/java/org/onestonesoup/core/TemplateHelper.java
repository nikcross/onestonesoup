package org.onestonesoup.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TemplateHelper {
	private static final String START_OF_PARAMETER = "&";
	private static final String END_OF_PARAMETER = ";";
	
	public static String generateStringWithTemplate(String template,Map<String,String> map) {
		String result = template;
		
		for(String key: map.keySet()) {
			String sub = START_OF_PARAMETER+key+END_OF_PARAMETER;
			//result = result.replaceAll(sub, map.get(key));
			while(result.indexOf(sub)!=-1) {
				result = result.replace(sub, map.get(key));
			}
		}
		
		return result;
	}
	public static Set<String> generateListForTemplate(String template) {
		Set<String> set = new HashSet<String>();
		
		while(template.length()>0) {
			String key = StringHelper.between(template, START_OF_PARAMETER, END_OF_PARAMETER);
			if(key==null) {
				break;
			}
			set.add(key);
			template = StringHelper.after(template,END_OF_PARAMETER);
		}
		
		return set;
	}
}
