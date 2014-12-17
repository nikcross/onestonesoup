package org.onestonesoup.core.printstreams;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class OutMultiplexer extends PrintStream {

	private static OutMultiplexer multiplexer;
	
	public Map<String,PrintStream> matchers = new HashMap<String,PrintStream>();
	private PrintStream defaultStream;
	private PrintStream originalDefaultStream;
	
	public PrintStream getDefaultStream() {
		return defaultStream;
	}

	public void setDefaultStream(PrintStream defaultStream) {
		this.defaultStream = defaultStream;
	}
	
	public void setDefaultStreamNull() {
		class NullOutputStream extends OutputStream {
			  @Override
			  public void write(int b) throws IOException {
			  }
			}
		
		this.defaultStream = new PrintStream( new NullOutputStream() );
	}
	
	public void resetDefaultStream() {
		this.defaultStream = this.originalDefaultStream;
	}

	private OutMultiplexer(OutputStream out) {
		super(out);
	}
	
	public static OutMultiplexer getMultiplexer() {
		
		if(multiplexer==null) {
			OutMultiplexerStream outStream = new OutMultiplexerStream();
			multiplexer = new OutMultiplexer(outStream);
			outStream.setParent(multiplexer);
			multiplexer.defaultStream = System.out;
			multiplexer.originalDefaultStream=System.out;
		}
		
		return multiplexer;
	}
	
	public void appendToDefaultOut(char c) {
		defaultStream.append(c);
	}
	
	public void printlnDefaultOut(String data) {
		defaultStream.println(data);
	}	

	public Map<String,PrintStream> getMatchers() {
		return matchers;
	}
	
	public void addMatcher(String matcher,PrintStream stream) {
		matchers.put(matcher,stream);
	}
	
	public void addClassMatcher(Class<?> clazz,PrintStream stream) {
		addMatcher( ".*"+clazz.getName().replace(".","\\.")+".*",stream );
	}
	
	public void addClassAndMethodMatcher(Class<?> clazz,String methodName,PrintStream stream) {
		addMatcher( ".*"+clazz.getName().replace(".","\\.")+":"+methodName+".*",stream );
	}
}
