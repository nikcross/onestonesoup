package org.onestonesoup.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.onestonesoup.core.NameHelper;

public class NameHelperTest {

	private String className = "SomeTestCaseTextIMHO";
	private String dataName = "someTestCaseTextIMHO";
	private String titleName = "Some Test Case Text IMHO";
	private String staticName = "SOME_TEST_CASE_TEXT_IMHO";
	
	@Test
	public void testCanConvertClassToTitle()
	{
		String result = NameHelper.classToTitleName(className);
		
		assertEquals(titleName,result);
	}
	@Test
	public void testCanConvertDataToStatic()
	{
		String result = NameHelper.dataToStaticName(dataName);
		
		assertEquals(staticName,result);		
	}
	@Test
	public void testCanConvertDataToTitle()
	{
		String result = NameHelper.dataToTitleName(dataName);
		
		assertEquals(titleName,result);		
	}
	@Test
	public void testCanConvertStaticToTitle()
	{
		String titleName = "Some Test Case Text Imho"; // not enough information to keep IMHO
		
		String result = NameHelper.staticToTitleName(staticName);
		
		assertEquals(titleName,result);	
	}
	@Test
	public void testCanConvertTitleToClass()
	{
		String result = NameHelper.titleToClassName(titleName);
		
		assertEquals(className,result);		
	}
	@Test
	public void testCanConvertTitleToData()
	{
		String result = NameHelper.titleToDataName(titleName);
		
		assertEquals(dataName,result);		
	}
	@Test
	public void testCanConvertTitleToStatic()
	{
		String result = NameHelper.titleToStaticName(titleName);
		
		assertEquals(staticName,result);		
	}
}
