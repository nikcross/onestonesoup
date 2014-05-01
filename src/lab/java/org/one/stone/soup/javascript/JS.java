package org.one.stone.soup.javascript;

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

import javax.script.ScriptException;

import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.core.javascript.JavascriptEngine;
import org.one.stone.soup.process.CommandLineTool;
import org.one.stone.soup.process.LogFile;
import org.one.stone.soup.process.SimpleLogFile;

public class JS extends CommandLineTool implements Runnable {

	public class JSInstance {
		public Object mountJar(String alias, String jarFile, String className)
				throws MalformedURLException, ClassNotFoundException,
				InstantiationException, IllegalAccessException {
			List<URL> urlList = new ArrayList<URL>();
			String jarFiles[] = jarFile.split(",");
			for (String file : jarFiles) {
				File jar = new File(file);
				URL jarURL = jar.toURI().toURL();
				urlList.add(jarURL);
			}
			URLClassLoader classLoader = new URLClassLoader(
					(URL[]) urlList.toArray(new URL[0]));
			classLoaders.put(alias, classLoader);
			if (className == null) {
				return null;
			} else {
				Class clazz = classLoader.loadClass(className);
				Object instance = clazz.newInstance();
				jsEngine.mount(alias, instance);
				return instance;
			}
		}

		public Object mount(String alias, String className) {
			try {
				Object obj;
				obj = Class.forName(className).newInstance();
				jsEngine.mount(alias, obj);
				return obj;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public Object run(String command) { 
			try{
				return jsEngine.runScript(command);
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		public Object runScript(String fileName) { 
			try{
				return jsEngine.runScript(FileHelper.loadFileAsString(fileName), fileName);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		public void runAsync(String command) { 
			new JSThread(command, null, jsEngine);
		}

		public void runScriptAsync(String fileName) throws IOException {
			String command = FileHelper.loadFileAsString(fileName);
			new JSThread(command, fileName, jsEngine);
		}

		public void sleep(long milliSeconds) throws InterruptedException {
			Thread.sleep(milliSeconds);
		}

		public void setCommandLog(String logFileName) {
			System.out.println("Logging commands to "+new File(logFileName).getAbsolutePath()+"TODO");
			//TODO
			//jsEngine.setLogFile(new SimpleLogFile(logFileName));
		}

		public void help() {
			String objects[] = jsEngine.getObjects();
			System.out.println("Objects:");
			for (String object: objects) {
				System.out.println(object);
			}
		}

		public void help(String name) {
			Object Object = jsEngine.getObject(name);
			help(Object);
		}

		public void help(Object Object) {
			//help(Object, null); ???
		}
	}

	class JSThread implements Runnable {
		public void run() {
			Object result;
			try {
				result = jsEngine.runScript(code);
				displayResult(result);
			} catch (ScriptException e) {
				e.printStackTrace();
			}
		}

		JavascriptEngine jsEngine;
		private String code;
		private String fileName;

		private JSThread(String code, String fileName, JavascriptEngine jsEngine) {
			super();
			this.code = code;
			this.fileName = fileName;
			this.jsEngine = jsEngine;
			(new Thread(this, "JS Thread")).start();
		}

		//JSThread(String code, String fileName, JavascriptEngine jsEngine, e e1) {
		//	this(code, fileName, jsEngine);
		//}
	}

	public static void main(String args[]) {
		new JS(args);
	}

	public JS(String args[]) {
		super(args);
		logFile = null;
	}

	public int getMinimumArguments() {
		return 0;
	}

	public int getMaximumArguments() {
		return 1;
	}

	public String getUsage() {
		return "[js-init-script-file]";
	}

	public void process() {
		jsEngine = new JavascriptEngine();
		js = new JSInstance();
		jsEngine.mount("js", js);
		jsEngine.mount("out", System.out);
		jsEngine.mount("err", System.err);
		try {
			String initScript = FileHelper.loadFileAsString(getClass()
					.getResourceAsStream("init.sjs"));
			if (initScript != null)
				try {
					jsEngine.runScript(initScript);
				} catch (ScriptException e) {
					e.printStackTrace();
				}
		} catch (IOException IOException) {
		}
		if (getParameter(0) != null)
			try {
				System.out.println((new StringBuilder("running ")).append(
						(new File(getParameter(0))).getAbsolutePath())
						.toString());
				jsEngine.runScript(
						FileHelper.loadFileAsString(getParameter(0)),
						getParameter(0));
			} catch (ScriptException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		if (!hasOption("noPrompt")) {
			thread = new Thread(this, "Javascript Engine");
			thread.start();
		}
	}

	public void run() {
		if (hasOption("log"))
			js.setCommandLog(getOption("log"));
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		try {
			System.out.print("JS> ");
			String line = reader.readLine();
			StringBuffer code = new StringBuffer();
			boolean bufferMode = false;
			for (; line != null; line = reader.readLine()) {
				try {
					if (!bufferMode) {
						if (line.equals("{{")) {
							bufferMode = true;
						} else {
							if (logFile != null)
								logFile.logMessage(line);
							Object result = jsEngine.runScript(line,
									"User Input");
							displayResult(result);
						}
					} else if (line.equals("}}")) {
						if (logFile != null)
							logFile.logMessage((new StringBuilder("{{\n"))
									.append(code.toString()).append("\n}}\n")
									.toString());
						Object result = jsEngine.runScript(code.toString(),
								"User Input");
						displayResult(result);
						code = new StringBuffer();
						bufferMode = false;
					} else if (line.equals("}}+")) {
						if (logFile != null)
							logFile.logMessage((new StringBuilder("{{\n"))
									.append(code.toString()).append("\n}}+\n")
									.toString());
						js.runAsync(code.toString());
						code = new StringBuffer();
						bufferMode = false;
					} else {
						code.append((new StringBuilder("\n")).append(line)
								.toString());
					}
				} catch (ScriptException e) {
					System.err.println(e.getMessage());
					code = new StringBuffer();
					bufferMode = false;
				}
				if (bufferMode)
					System.out.print("JS+ ");
				else
					System.out.print("JS> ");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void displayResult(Object result) {
		if (result == null)
			return;
		if ((result instanceof String) || (result instanceof Integer)
				|| (result instanceof Double))
			System.out.println(result.toString());
	}

	private JavascriptEngine jsEngine;
	private JSInstance js;
	private Thread thread;
	private static Map<String, ClassLoader> classLoaders = new HashMap<String, ClassLoader>();
	private LogFile logFile;
}
