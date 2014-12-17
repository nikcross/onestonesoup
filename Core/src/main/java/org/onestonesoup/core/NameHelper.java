package org.onestonesoup.core;

import java.util.*;

/*
 * Wet-Wired.com Library Version 2.1
 *
 * Copyright 2000-2001 by Wet-Wired.com Ltd.,
 * Portsmouth England
 * This software is OSI Certified Open Source Software
 * This software is covered by an OSI approved open source licence
 * which can be found at http://www.onestonesoup.org/OSSLicense.html
 */

/**
	* The JavaNameHelper supplies a number of static methods for converting between different Java
	* naming convetions. It converts between a title, class names, data names and static names.
	* Conversion between any of the naming conventions is possible going via a title string.
	*
	* Below is the list of possible conversions and the method calls required.
	*
	* data to static		dataToTitleName then titleToStaticName
	* data to class			dataToTitleName then titleToClassName
	* data to static		dataToTitleName then titleToStaticName
	* static to data		staticToTitleName then titleToDataName
	* static to class		staticToTitleName then titleToClassName
	* static to title		staticToTitleName
	* class to static		classToTitleName then titleToStaticName
	* class to title		classToTitleName
	* title to data			titleToDataName
	* title to static		titleToStaticName
	* title to class		titleToClassName
	*
	* @author Nik Cross
*/

public class NameHelper {
/**
	* Converts a class name to a title.
	* If name = "MyClassName" the returned string is "My class name"
	* @param name	The string to be transformed
*/
public static String classToTitleName(String name) {
	return dataToTitleName(name);
}
/**
	* Converts a data name to a title.
	* If name = "myDataName" the returned string is "My data name"
	* @param name	The string to be transformed
*/

public static String dataToStaticName(String name) {

	return titleToStaticName( dataToTitleName( name ) );
}
/**
	* Converts a data name to a title.
	* If name = "myDataName" the returned string is "My data name"
	* @param name	The string to be transformed
*/

public static String dataToTitleName(String name) {

	StringBuffer buffer = new StringBuffer();
	StringBuffer word = new StringBuffer();

	for(int loop=0;loop<name.length();loop++)
	{
		if( loop!=0 && Character.isUpperCase( name.charAt(loop) ) )
		{
			buffer.append( word.toString() );
			if(word.length()>1) // handle acronym
			{
				buffer.append( ' ' );
			}
			word = new StringBuffer();
		}

		if(word.length()==0)
			word.append( Character.toUpperCase( name.charAt(loop) ) );
		else
			word.append( Character.toLowerCase( name.charAt(loop) ) );
	}

	buffer.append( word.toString() );

	return buffer.toString();
}
/**
	* Replaces non-alphanumerics with a space.
	* If name = "My-class-name" the returned string is "My Class Name"
	* @param data	The string to be transformed
*/

private static String removeNonAlphas(String data) {

	StringBuffer buffer = new StringBuffer();
	
	for(int loop=0;loop<data.length();loop++)
	{
		if( Character.isLetterOrDigit( data.charAt(loop) ) )
		{
			buffer.append(data.charAt(loop));
		}
		else
		{
			buffer.append( ' ' );
		}
	}

	return buffer.toString();
}
/**
	* Converts a static name to a title.
	* If name = "MY_STATIC_NAME" the returned string is "My static name"
	* @param name	The string to be transformed
*/

public static String staticToTitleName(String name) {
	name = name.toLowerCase();
	name = name.replace('_',' ');
	
	StringTokenizer tokenizer = new StringTokenizer( name," ",true );

	StringBuffer buffer = new StringBuffer();

	while( tokenizer.hasMoreTokens() )
	{
		String word = tokenizer.nextToken();
		String wordCaps = word.toUpperCase();
		buffer.append(wordCaps.charAt(0));
		buffer.append(word.substring(1));
	}
	
	return buffer.toString();

}
/**
	* Converts a title to a class name.
	* If name = "My class name" the returned string is "MyClassName"
	* @param name	The string to be transformed
*/

public static String titleToClassName(String name) {

	name = removeNonAlphas(name);

	StringTokenizer tokenizer = new StringTokenizer( name," " );

	StringBuffer buffer = new StringBuffer();

	while( tokenizer.hasMoreTokens() )
	{
		String word = tokenizer.nextToken();
		String wordCaps = word.toUpperCase();
		buffer.append(wordCaps.charAt(0));
		buffer.append(word.substring(1));
	}

	return buffer.toString();
}
/**
	* Converts a title to a data name.
	* If name = "My data name" the returned string is "myDataName"
	* @param name	The string to be transformed
*/
public static String titleToDataName(String name) {

	name = removeNonAlphas(name);

	StringTokenizer tokenizer = new StringTokenizer( name," " );

	StringBuffer buffer = new StringBuffer();

	boolean firstWord = true;
	
	while( tokenizer.hasMoreTokens() )
	{
		String word = tokenizer.nextToken();
		if(firstWord)
		{
			buffer.append(word.toLowerCase().charAt(0));
			buffer.append(word.substring(1));
			firstWord=false;
		}
		else
		{
			buffer.append(word);
		}
	}

	return buffer.toString();
}
/**
	* Converts a title to a static name.
	* If name = "My static name" the returned string is "MY_STATIC_NAME"
	* @param name	The string to be transformed
*/

public static String titleToStaticName(String name) {

	name = removeNonAlphas(name);

	name = name.toUpperCase();
	name = name.replace( ' ','_' );

	return name;
}
}
