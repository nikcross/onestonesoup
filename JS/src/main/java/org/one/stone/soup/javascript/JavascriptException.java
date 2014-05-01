package org.one.stone.soup.javascript;

public class JavascriptException extends Exception {
	
	private int columnNumber;
	private String details;
	private int lineNumber;

	public JavascriptException(String details,int lineNumber,int columnNumber) {
		super(details+" at:"+lineNumber+":"+columnNumber);
		this.details = details;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	public String getDetails() {
		return details;
	}

	public int getLineNumber() {
		return lineNumber;
	}
}
