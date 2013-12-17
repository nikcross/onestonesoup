package org.one.stone.soup.javascript.helper;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.mozilla.javascript.NativeJavaClass;
import org.one.stone.soup.javascript.JS;

public class JSHelp {

	public static String help(Object object,String name) {
		StringBuffer buffer = new StringBuffer();
		
		Class clazz = object.getClass();
		if( object instanceof NativeJavaClass ) {
			NativeJavaClass njc = (NativeJavaClass)object;
			clazz = njc.getClassObject();
		}

		if(name==null) {
			name = JS.getInstance().getObjectAlias(object);
			if(name==null) {
				name = "THING";
			}
		}
		
		Method[] methods = clazz.getDeclaredMethods();
		buffer.append("("+clazz+") "+name+"\n");
		for(Method method: methods) {
			if(Modifier.isPublic(method.getModifiers())==false) {
				continue;
			}
			if(method.getAnnotation(JSMethodHelp.class)!=null) {
				JSMethodHelp jsMethodHelp = (JSMethodHelp)method.getAnnotation(JSMethodHelp.class);
				buffer.append(name+"."+method.getName()+"("+jsMethodHelp.signature()+")\n");
				continue;
			}
			String methodLine = name+"."+method.getName()+"(";
			Class<?>[] params = method.getParameterTypes();
			String paramString = "";
			for(Class param: params) {
				if(paramString.length()>0) {
					paramString+=", ";
				}
				paramString+=param.getSimpleName();
			}
			methodLine += paramString;
			methodLine+=")";
			methodLine+=" "+method.getReturnType().getSimpleName();
			
			buffer.append( methodLine+"\n" );
		}
		return buffer.toString();
	}
}
