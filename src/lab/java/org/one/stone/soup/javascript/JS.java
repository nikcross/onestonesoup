package org.one.stone.soup.javascript;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.one.stone.soup.core.DirectoryHelper;
import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.javascript.helper.JSHelp;
import org.one.stone.soup.process.CommandLineTool;
import org.one.stone.soup.process.LogFile;
import org.one.stone.soup.process.SimpleLogFile;

public class JS extends CommandLineTool implements Runnable{

	private static JS js;
	private static String[] initArgs = new String[]{};
	
	public static void main(String[] args) {
		initArgs = args;
		getInstance();
	}
	
	public static JSInterface getInstance() {
		if(js==null) {
			js = new JS(initArgs);
		}
		return js.jsInterface;
	}
	
	private JS(String[] args) {
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
	private JSInterface jsInterface;
	private Thread thread;
	private static Map<String,URLClassLoader> classLoaders = new HashMap<String,URLClassLoader>();
	private LogFile logFile = null;
	
	public class JSInterface {
		private JavascriptEngine jsEngine;
		public JSInterface(JavascriptEngine jsEngine) {
			this.jsEngine = jsEngine;
		}
		
		public Object mountJar(String alias,String jarFile,String className) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
			
			List<URL> urlList = new ArrayList<URL>();
			String[] jarFiles = jarFile.split(",");
			for(String file: jarFiles) {		
				File jar = new File(file);
				if(jar.exists()==false) {
					System.out.println("Jar "+jar.getAbsolutePath()+" does not exist.");
				}
				if(jar.isDirectory()) {
					List<File> jars = DirectoryHelper.findFiles(jar.getAbsolutePath(), ".*\\.jar", true);
					for(File j: jars) {
						System.out.println("added jar "+j.getAbsolutePath());
						URL jarURL = j.toURI().toURL();
						urlList.add(jarURL);
					}
				} else {
					System.out.println("added jar "+jar.getAbsolutePath());
					URL jarURL = jar.toURI().toURL();
					urlList.add(jarURL);
				}
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
		
		public void mountObject(String alias,Object object) {
				jsEngine.mount(alias,object);
		}
		
		public String getObjectAlias(Object object) {
			return jsEngine.getObjectKey(object);
		}
		
		public Object run(String command) {
			try {
				return jsEngine.runScript(command);
			} catch (JavascriptException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public Object runScript(String fileName) {
			try {
				return jsEngine.runScript( FileHelper.loadFileAsString(fileName),fileName );
			} catch (JavascriptException e) {
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
		
		public JavascriptEngine getEngine(String scopeName) {
			return jsEngine.getEngine(scopeName);
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
			System.out.println(JSHelp.help(object, name));
		}
		
		public boolean isHeadless() {
			return GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadless();
		}
		
		public void exit() {
			System.exit(0);
		}

		public Object getObject(String name) {
			return jsEngine.getObject(name);
		}
	}
	
	private class JSThread implements Runnable{
		JSInterface jsEngine;
		private String code;
		private String fileName;
		
		private JSThread(String code,String fileName,JSInterface jsEngine) {
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
		jsEngine = JavascriptEngine.getInstance();
		jsInterface = new JSInterface(this.jsEngine);
		jsEngine.mount("js",jsInterface);
		jsEngine.mount("out",System.out);
		jsEngine.mount("err",System.err);
		
		try {
			String initScript = FileHelper.loadFileAsString( this.getClass().getResourceAsStream("init.sjs") );
			if(initScript!=null) {
				try {
					jsEngine.runScript( initScript );
				} catch (JavascriptException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
		}
		
		
		if(getParameter(0)!=null) {
			try {
				System.out.println("running "+new File(getParameter(0)).getAbsolutePath());
				jsEngine.runScript( FileHelper.loadFileAsString(getParameter(0)),getParameter(0) );
			} catch (JavascriptException e) {
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
			jsInterface.setCommandLog( getOption("log") );
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
							jsInterface.runAsync(code.toString());
							code=new StringBuffer();
							bufferMode=false;
						} else {
							code.append("\n"+line);
						}
					}
				} catch (Throwable e) {
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
