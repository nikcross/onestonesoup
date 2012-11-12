package org.one.stone.soup.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class JSONHelperTest {

	public class TestClass1 {
		private String alpha;
		private int beta;
		private TestClass1 theta;
		public String getAlpha() {
			return alpha;
		}
		public void setAlpha(String alpha) {
			this.alpha = alpha;
		}
		public int getBeta() {
			return beta;
		}
		public void setBeta(int beta) {
			this.beta = beta;
		}
		public TestClass1 getTheta() {
			return theta;
		}
		public void setTheta(TestClass1 theta) {
			this.theta = theta;
		}
	}
	
	@Test
	public void testCanConvertObjectToJSON() {
		
		TestClass1 instance = new TestClass1();
		instance.setAlpha("alpha");
		instance.setBeta(1);
		
		String expectedJSON = "{alpha: \"alpha\",beta: 1}";
		
		String json = JSONHelper.toJSON(instance);
		
		assertEquals(expectedJSON,json);
	}
}
