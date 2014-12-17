package org.onestonesoup.core;

public class RegExBuilder {

	StringBuffer expression = new StringBuffer();
	
	public static RegExBuilder regex() {
		return new RegExBuilder();
	}
	
	public RegExBuilder findAnyString() {
		expression.append(".*");
		return this;
	}
	
	public RegExBuilder and() {
		RegExBuilder newRegEx = regex();
		newRegEx.expression.append("("+expression+")&&");
		return newRegEx;
	}
	
	public RegExBuilder or() {
		RegExBuilder newRegEx = regex();
		newRegEx.expression.append("("+expression+")||");
		return newRegEx;
	}
	public RegExBuilder dont() {
		expression.insert(0, "^");
		return this;
	}	
	public RegExBuilder dont(RegExBuilder regex) {
		expression.append( "[^"+regex.toRegEx()+"]");
		return this;
	}	
	
	public RegExBuilder findTheString(String string) {
		expression.append("("+string+")");
		return this;
	}
	
	public RegExBuilder findACharcterInTheSet(String characters) {
		expression.append("["+characters+"]");
		return this;
	}
	
	public RegExBuilder findAnAlphaCharacter() {
		expression.append("[a-zA-Z]");
		return this;
	}
	
	public RegExBuilder findAnAlphaNumericCharacter() {
		expression.append("[a-zA-Z 0-9]");
		return this;
	}
	
	public RegExBuilder findADigit() {
		expression.append("\\d");
		return this;
	}
	
	public RegExBuilder findANonDigit() {
		expression.append("\\D");
		return this;
	}
	
	public RegExBuilder findASpace() {
		expression.append("\\s");
		return this;
	}
	
	public RegExBuilder findANonSpace() {
		expression.append("\\S");
		return this;
	}
	
	public RegExBuilder findAWordCharacter() {
		expression.append("\\w");
		return this;
	}
	
	public RegExBuilder findANonWord() {
		expression.append("\\W");
		return this;
	}
	
	public RegExBuilder findACharacter() {
		expression.append(".");
		return this;
	}
	
	public RegExBuilder findNTimes(int n) {
		expression.append("{"+n+"}");
		return this;
	}
	
	public RegExBuilder cardinality(RegExBuilder target,int minNumber,int maxNumber) {
		expression.append("("+target.toRegEx()+"){"+minNumber+","+maxNumber+"}");
		return this;
	}
	
	public RegExBuilder cardinality(int minNumber,int maxNumber) {
		expression.append("{"+minNumber+","+maxNumber+"}");
		return this;
	}
	
	public String toRegEx() {
		return expression.toString();
	}
	
	public boolean matches(String data) {
		boolean result = data.matches(expression.toString());
		if( result ) {
			System.out.println(data+" matches \""+toRegEx()+"\"");
		} else {
			System.out.println(data+" does NOT match \""+toRegEx()+"\"");
		}
		
		return result;
	}

	public RegExBuilder onlyOnce() {
		expression.append("{1}");
		return this;
	}
	public RegExBuilder atLeastOnce() {
		expression.append("{1,}");
		return this;
	}
}
