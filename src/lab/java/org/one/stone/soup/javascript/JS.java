package org.one.stone.soup.javascript;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.core.javascript.JavascriptEngine;
import org.one.stone.soup.process.CommandLineTool;
import org.one.stone.soup.process.LogFile;
import org.one.stone.soup.process.SimpleLogFile;

import sun.org.mozilla.javascript.internal.NativeFunction;
import sun.org.mozilla.javascript.internal.NativeJavaClass;
import sun.org.mozilla.javascript.internal.NativeObject;

public class JS extends CommandLineTool implements Runnable{

	public static void main(String[] args) {
		new JS(args);
	}
	
	public JS(String[] args) {
		super(args);
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public int getMaximumArguments() {
		return 1;
	}

	@Override
	public String getUsage() {
		return "[js-init-script-file]";
	}

	private JavascriptEngine jsEngine;
	private JSInstance js;
	private Thread thread;
	private static Map<String,URLClassLoader> classLoaders = new HashMap<String,URLClassLoader>();
	private LogFile logFile = null;
	
	public class JSInstance {
		public Object mountJar(String alias,String jarFile,String className) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
			
			List<URL> urlList = new ArrayList<URL>();
			String[] jarFiles = jarFile.split(",");
			for(String file: jarFiles) {		
				File jar = new File(file);
				URL jarURL = jar.toURI().toURL();
				urlList.add(jarURL);
			}
			
			URLClassLoader classLoader = new URLClassLoader( urlList.toArray(new URL[]{}) );
			classLoaders.put(alias,classLoader);
			
			if(className==null) {
				return null;
			} else {
				Class clazz = classLoader.loadClass(className);
				Object instance = clazz.newInstance();
				jsEngine.mount(alias,instance);
				return instance;
			}
		}
		
		public Object mount(String alias,String className) {
			Object obj;
			try {
				obj = Class.forName(className).newInstance();
				jsEngine.mount(alias,obj);
				return obj;
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public Object run(String command) {
			try {
				return jsEngine.runScript(command);
			} catch (ScriptException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public Object runScript(String fileName) {
			try {
				return jsEngine.runScript( FileHelper.loadFileAsString(fileName),fileName );
			} catch (ScriptException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public void runAsync(String command) {
			new JSThread(command,null,this);
		}
		
		public void runScriptAsync(String fileName) throws IOException {
			String command = FileHelper.loadFileAsString(fileName);
			new JSThread(command,fileName,this);
		}
		
		public void sleep(long milliSeconds) throws InterruptedException {
			Thread.sleep(milliSeconds);
		}
		
		public void setCommandLog(String logFileName) {
			System.out.println( "Logging commands to "+new File(logFileName).getAbsolutePath() );
			logFile = new SimpleLogFile(logFileName);
		}
		
		public void help() {
			String[] objects = jsEngine.getObjects();
			
			System.out.println( "Objects:" );
			
			for(String object: objects) {
				System.out.println( "  "+object );
			}
		}
		
		public void help(String name) {
			Object object = jsEngine.getObject(name);
			help(object);
		}
		
		public void help(Object object) {
			help(object,null);
		}
		
		private void help(Object object,String name) {
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
		
		public void exit() {
			System.exit(0);
		}
	}
	
	private class JSThread implements Runnable{
		JSInstance jsEngine;
		private String code;
		private String fileName;
		
		private JSThread(String code,String fileName,JSInstance jsEngine) {
			this.code = code;
			this.fileName = fileName;
			this.jsEngine = jsEngine;
			new Thread(this,"JS Thread").start();
		}
		public void run() {
			Object result = jsEngine.run(code);
			displayResult(result);
		}
	}
	
	@Override
	public void process() {
		jsEngine = new JavascriptEngine();
		js = new JSInstance();
		jsEngine.mount("js",js);
		jsEngine.mount("out",System.out);
		jsEngine.mount("err",System.err);
		
		try {
			String initScript = FileHelper.loadFileAsString( this.getClass().getResourceAsStream("init.sjs") );
			if(initScript!=null) {
				try {
					jsEngine.runScript( initScript );
				} catch (ScriptException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
		}
		
		
		if(getParameter(0)!=null) {
			try {
				System.out.println("running "+new File(getParameter(0)).getAbsolutePath());
				jsEngine.runScript( FileHelper.loadFileAsString(getParameter(0)),getParameter(0) );
			} catch (ScriptException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(hasOption("noPrompt")==false) {
			thread = new Thread(this,"Javascript Engine");
			thread.start();
		}
	}
	
	public void run() {
		
		if(hasOption("log")) {
			js.setCommandLog( getOption("log") );
		}
		
		BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) );
		try {
			System.out.print("JS> ");
			String line = reader.readLine();
			StringBuffer code = new StringBuffer();
			boolean bufferMode=false;
			
			while(line != null) {
				try {
					if(bufferMode==false) {
						if(line.equals("{{")) {
							bufferMode=true;
						} else {
							if(logFile!=null) {
								logFile.logMessage(line);
							}
							Object result = jsEngine.runScript(line,"User Input");
							displayResult(result);
						}
					} else {
						if(line.equals("}}")) {
							if(logFile!=null) {
								logFile.logMessage("{{\n"+code.toString()+"\n}}\n");
							}
							Object result = jsEngine.runScript(code.toString(),"User Input");
							displayResult(result);
							code=new StringBuffer();
							bufferMode=false;
						} else if(line.equals("}}+")) {
							if(logFile!=null) {
								logFile.logMessage("{{\n"+code.toString()+"\n}}+\n");
							}
							js.runAsync(code.toString());
							code=new StringBuffer();
							bufferMode=false;
						} else {
							code.append("\n"+line);
						}
					}
				} catch (ScriptException e) {
					System.err.println(e.getMessage());
					code=new StringBuffer();
					bufferMode=false;
				}
				
				if(bufferMode) {
					System.out.print("JS+ ");
				} else {
					System.out.print("JS> ");
				}
				line = reader.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void displayResult(Object result) {
		if(result==null) return;
		if(
				result instanceof String ||
				result instanceof Integer ||
				result instanceof Double
		) {
			System.out.println(result.toString());
		}
	}
}
