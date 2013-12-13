package org.one.stone.soup.javascript.helper;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class JSHelp {

	public static String help(Object object,String name) {
		Class clazz = object.getClass();
		if( object instanceof NativeJavaClass ) {
			NativeJavaClass njc = (NativeJavaClass)object;
			clazz = njc.getClassObject();
		}
	//		System.out.println("Sorry. No help available for this.");
	//		return;
	//	}
		if(name==null) {
			name = jsEngine.getObjectKey(object);
			if(name==null) {
				name = "THING";
			}
		}
		
		Method[] methods = clazz.getDeclaredMethods();
		System.out.println("("+clazz+") "+name);
		for(Method method: methods) {
			if(Modifier.isPublic(method.getModifiers())==false) {
				continue;
			}
			String methodLine = name+"."+method.getName()+"(";
			Class<?>[] params = method.getParameterTypes();
			for(Class param: params) {
				methodLine+=param.getSimpleName()+", ";
			}
			methodLine+=")";
			methodLine+=" "+method.getReturnType().getSimpleName();
			
			System.out.println( methodLine );
		}
	}
}
