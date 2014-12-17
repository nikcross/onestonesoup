package org.onestonesoup.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onestonesoup.core.StringHelper;

public class StringHelperTest {

	@Test
	public void testLongStringIsTruncated() {
		String result = StringHelper.truncate("This is a long string", 10, "...");
		assertEquals( "This is...", result );
	}
	
	@Test
	public void testShortStringIsNotTruncated() {
		String result = StringHelper.truncate("This is short", 20, "...");
		assertEquals( "This is short",result );
	}
	
	@Test
	public void testSplitCanIgnorMarkersInsideRegions() {
		String[] result = StringHelper.split("one,two,three[and,this],four",
				",", "[", "]");
		assertEquals(4,result.length);
		assertEquals( "one",result[0] );
		assertEquals( "two",result[1] );
		assertEquals( "three[and,this]",result[2] );
		assertEquals( "four",result[3] );
		
	}
	
	@Test
	public void testSplitCanIgnorMarkersInsideQuotes() {
		String[] result = StringHelper.split("this is \"a , quote\"",
				",", "\"", "\"");
		assertEquals(1,result.length);
		assertEquals( "this is \"a , quote\"",result[0] );
		
	}
}
