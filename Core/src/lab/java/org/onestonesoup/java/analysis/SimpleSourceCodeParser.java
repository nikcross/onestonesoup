package org.onestonesoup.java.analysis;

/**
 * 
 * A class designed to provide the structure of a class from it's source so that it can be modeled
 * without having to be compiled.
 *
 */

public class SimpleSourceCodeParser {
	
	public SourceCode parseSourceCode(String source) {
		SourceCode sourceCode = new SourceCode(source);
		return sourceCode;
	}
}
