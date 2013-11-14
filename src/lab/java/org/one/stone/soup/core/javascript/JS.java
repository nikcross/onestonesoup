package org.one.stone.soup.core.javascript;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.script.ScriptException;

import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.process.CommandLineTool;

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
	private Thread thread;
	private BufferedReader reader;
	
	public class JSEngine {
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
	}
	
	@Override
	public void process() {
		jsEngine = new JavascriptEngine();
		jsEngine.mount("js",new JSEngine());
		jsEngine.mount("out",System.out);
		jsEngine.mount("err",System.err);
		
		if(getParameter(0)!=null) {
			try {
				jsEngine.runScript( FileHelper.loadFileAsString(getParameter(0)),getParameter(0) );
			} catch (ScriptException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		thread = new Thread(this,"Javascript Engine");
		thread.start();
	}
	
	public void run() {
		
		BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) );
		try {
			System.out.print("JS> ");
			String line = reader.readLine();
			
			while(line != null) {
				try {
					jsEngine.runScript(line,"User Input");
				} catch (ScriptException e) {
					System.err.println(e.getMessage());
				}
				
				System.out.print("JS> ");
				line = reader.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
