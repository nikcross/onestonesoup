package org.one.stone.soup.javascript;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.core.javascript.JavascriptEngine;
import org.one.stone.soup.process.CommandLineTool;
import org.one.stone.soup.process.LogFile;
import org.one.stone.soup.process.SimpleLogFile;

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
		public Object mountJar(String alias,String jarFile,String className) throws MalformedURLException, ClassNotFoundException {
			File jar = new File(jarFile);
			URL jarURL = jar.toURI().toURL();
			
			URLClassLoader classLoader = new URLClassLoader( new URL[]{jarURL} );
			classLoaders.put(alias,classLoader);
			
			if(className==null) {
				return null;
			} else {
				classLoader.loadClass(className);
				return mount(alias,className);
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
			jsEngine.run(code);
		}
	}
	
	@Override
	public void process() {
		jsEngine = new JavascriptEngine();
		js = new JSInstance();
		jsEngine.mount("js",js);
		jsEngine.mount("out",System.out);
		jsEngine.mount("err",System.err);
		
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
		
		if(hasOption("log")) {
			js.setCommandLog( getOption("log") );
		}
		
		if(hasOption("noPrompt")==false) {
			thread = new Thread(this,"Javascript Engine");
			thread.start();
		}
	}
	
	public void run() {
		
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
							jsEngine.runScript(line,"User Input");
						}
					} else {
						if(line.equals("}}")) {
							if(logFile!=null) {
								logFile.logMessage("{{\n"+code.toString()+"\n}}\n");
							}
							jsEngine.runScript(code.toString(),"User Input");
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
}
