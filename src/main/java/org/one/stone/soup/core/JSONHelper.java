package org.one.stone.soup.core;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nikcross
 *
 */
public class JSONHelper {
	/**
	 *
	 */
	private JSONHelper() {
		super();
	}

	public static String toJSON(Object instance)
	{
		if(instance==null)
		{
			instance = new NullPointerException();
		}else if(instance instanceof List) {
			Object[] list = ((List) instance).toArray();
 			
			if(list.length>0 && list[0] instanceof String) {
				String[] strings = new String[list.length];
				for(int i=0;i<list.length;i++) {
					strings[i] = (String)list[i];
				}
				instance = strings;
			} else {
				instance = list;
			}
		}
		
		StringBuffer buffer = new StringBuffer("{");

		@SuppressWarnings("rawtypes")
		Class clazz = instance.getClass();
		
		StringBuffer value = new StringBuffer();
		if( tryToAppendValue(value,instance)==true ) {
			buffer.append("value: ");
			buffer.append(value);
		}

		List<Method> getters = getGetters(clazz,true);

		boolean isFirst = true;
		for(Method getter: getters)
		{
			try{
				Object attribute = getter.invoke(instance,new Object[]{});

				if(attribute==null)
				{
					continue;
				}

				String attributeName = getter.getName().substring(3);
				attributeName = NameHelper.classToTitleName( attributeName );
				attributeName = NameHelper.titleToDataName( attributeName );
				
				if(isFirst) {
					isFirst=false;
				} else {
					buffer.append(",");
				}
				buffer.append(attributeName+": ");
				if( tryToAppendValue(buffer,attribute)==false ) {
					buffer.append( toJSON(attribute) );
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		buffer.append("}");
		
		return buffer.toString();
	}

	private static boolean tryToAppendValue(StringBuffer buffer, Object instance) {
		int length = buffer.length();
		if(
				instance instanceof String
		)
		{
			buffer.append("\""+(String)instance+"\"");
		}
		else if(
				instance instanceof String[]
		)
		{
			buffer.append("[");
			int count = Array.getLength(instance);
			for(int loopA=0;loopA<count;loopA++)
			{
				Object item = Array.get(instance,loopA);
				if(count>0) {
					buffer.append(",");
				}
				buffer.append("\""+(String)item+"\"");
			}
			buffer.append("]");
		}				
		else if(
				instance instanceof Boolean ||
				instance instanceof Integer ||
				instance instanceof Long ||
				instance instanceof Double
		)
		{
			buffer.append( ""+instance );
		}
		else if(
				instance instanceof Boolean[] ||
				instance instanceof Integer[] ||
				instance instanceof Long[] ||
				instance instanceof Double[]
		)
		{
			buffer.append("[");
			int count = Array.getLength(instance);
			for(int loopA=0;loopA<count;loopA++)
			{
				Object item = Array.get(instance,loopA);
				if(count>0) {
					buffer.append(",");
				}
				buffer.append(""+item);
			}
			buffer.append("]");
		}
		else if(instance.getClass().isArray())
		{
			buffer.append("[");
			int count = Array.getLength(instance);
			for(int loopA=0;loopA<count;loopA++)
			{
				Object item = Array.get(instance,loopA);
				if(loopA>0) {
					buffer.append(",");
				}
				buffer.append(toJSON(item));
			}
			buffer.append("]");
		}
		if(buffer.length()==length) {
			return false;
		} else {
			return true;
		}
	}

	private static List<Method> getGetters(@SuppressWarnings("rawtypes") Class clazz,boolean includeThoseWithoutSetters)
	{
		Method[] methods = clazz.getMethods();

		List<Method> testGetters = new ArrayList<Method>();
		for(Method method: methods) {
			if(
					method.getName().substring(0,3).equals("get") &&
					method.getParameterTypes().length==0 &&
					Modifier.isPublic( method.getModifiers() )==true &&
					method.getName().equals("getClass")==false ) {
				testGetters.add(method);
			}
		}

		if(includeThoseWithoutSetters==true) {
			return testGetters;
		}
		
		List<Method> getters = new ArrayList<Method>();
		// Check for matching setter
		for(Method method: testGetters)
		{
			@SuppressWarnings("rawtypes")
			Class returnType = method.getReturnType();
			String setterName = "s"+method.getName().substring(1);
			try{
				@SuppressWarnings("unchecked")
				Method setter = clazz.getMethod(setterName,new Class[]{returnType});
				if( Modifier.isPublic(setter.getModifiers())==true )
				{
					getters.add(method);
				}
			}
			catch(NoSuchMethodException me){}
		}

		return getters;
	}
}
