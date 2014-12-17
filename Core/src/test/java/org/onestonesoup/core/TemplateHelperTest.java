package org.onestonesoup.core;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.onestonesoup.core.TemplateHelper;

public class TemplateHelperTest {

	@Test
	public void testTemplateSubsitiutionsMade() {
		String template = "A &animal; went to &place;. The &animal; ate well.";
		Map<String,String> substitutions = new HashMap<String,String>();
		substitutions.put("animal","snail");
		substitutions.put("place","Halcyon");
		
		String result = TemplateHelper.generateStringWithTemplate(template,substitutions);
		
		assertEquals("A snail went to Halcyon. The snail ate well.",result);
	}
	
	@Test
	public void testTemplateSubsitutionsMadeWhenSlashInToken() {
		String template = "The &insert:/OpenForum/TopMenu; today is.";
		Map<String,String> substitutions = new HashMap<String,String>();
		
		substitutions.put("insert:/OpenForum/TopMenu","Cheese Soup");
		
		String result = TemplateHelper.generateStringWithTemplate(template,substitutions);
		
		assertEquals("The Cheese Soup today is.",result);
		
	}
	
	@Test
	public void testTemplateSubstitionsExtracted() {
		String template = "A &animal; went to &place;. The &animal; ate well.";

		Set<String> list = TemplateHelper.generateListForTemplate(template);
		
		assertTrue("Animal substitution not found",list.contains("animal"));
		assertTrue("Place substitution not found",list.contains("place"));
	}
}
