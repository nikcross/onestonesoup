package org.one.stone.soup.core.printstreams;

import java.io.IOException;
import java.io.OutputStream;

public class OutMultiplexerStream extends OutputStream{
	
	private OutMultiplexer parent;
	OutMultiplexerStream() {}
	
	public void setParent(OutMultiplexer parent) {
		this.parent=parent;
	}
	
	@Override
	public void write(int b) throws IOException {
		StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
		StringBuffer path = new StringBuffer();
		for(int i = traceElements.length-1;i>=0;i--) {
			if(traceElements[i].getClassName().equals("java.io.PrintStream")) {
				break;
			}
			
			path.append(traceElements[i].getClassName());
			path.append(":");
			path.append(traceElements[i].getMethodName());
			path.append(">");
		}
		String pathString = path.toString();
		
		for(String matcher: parent.getMatchers().keySet()) {
			if(pathString.matches(matcher)) {
				parent.getMatchers().get(matcher).append((char)b);
				return;
			}
		}
		parent.appendToDefaultOut((char)b);
	}
	
}
