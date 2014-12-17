package org.onestonesoup.java.analysis;

import java.util.ArrayList;
import java.util.List;

public class SourceCode {

	public class MethodMarker {
		public String signiture;
		public int startPosition;
		public int endPosition;
	}
	
	private List<MethodMarker> methods = new ArrayList<MethodMarker>();
	private String source;
	
	public SourceCode(String source) {
		this.source = source;
	}
}