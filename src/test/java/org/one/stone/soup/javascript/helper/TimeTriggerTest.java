package org.one.stone.soup.javascript.helper;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.javascript.trigger.ScheduledTask;
import org.one.stone.soup.javascript.trigger.TimeTrigger;

public class TimeTriggerTest {

	/*private static final String CONFIG = "instance { "+
			"class: \"org.one.stone.soup.javascript.helper.TimeTriggerConfiguration\", "+
			"attribute { name: \"scheduledTasks\", class: \"[Lorg.one.stone.soup.javascript.helper.ScheduledTask;\", "+
			"instance { class: \"org.one.stone.soup.javascript.helper.ScheduledTask\", "+
				"attribute { name: \"script\", class: \"java.lang.String\"}, "+
				"attribute { name: \"hour\", value: \"13\", class: \"java.lang.Integer\"}, "+
				"attribute { name: \"minute\", value: \"27\", class: \"java.lang.Integer\"}, "+
				"attribute { name: \"alias\", class: \"java.lang.String\"}}, "+
			"instance { class: \"org.one.stone.soup.javascript.helper.ScheduledTask\", "+
				"attribute { name: \"script\", class: \"java.lang.String\"}, "+
				"attribute { name: \"hour\", value: \"12\", class: \"java.lang.Integer\"}, "+
				"attribute { name: \"minute\", value: \"27\", class: \"java.lang.Integer\"}, "+
				"attribute { name: \"alias\", class: \"java.lang.String\"}}}}";*/
	
	private static final String CONFIG = 
	"<instance class=\"org.one.stone.soup.javascript.helper.TimeTriggerConfiguration\">\n"+
	"	<attribute name=\"scheduledTasks\" class=\"[Lorg.one.stone.soup.javascript.helper.ScheduledTask;\">\n"+
	"		<instance class=\"org.one.stone.soup.javascript.helper.ScheduledTask\">\n"+
	"			<attribute name=\"script\" class=\"java.lang.String\">println(\"Testing\");</attribute>\n"+
	"			<attribute name=\"hour\" value=\"13\" class=\"java.lang.Integer\"/>\n"+
	"			<attribute name=\"minute\" value=\"27\" class=\"java.lang.Integer\"/>\n"+
	"			<attribute name=\"alias\" class=\"java.lang.String\">test task2</attribute>\n"+
	"		</instance>\n"+
	"		<instance class=\"org.one.stone.soup.javascript.helper.ScheduledTask\">\n"+
	"			<attribute name=\"script\" class=\"java.lang.String\">println(\"Testing\");</attribute>\n"+
	"			<attribute name=\"hour\" value=\"12\" class=\"java.lang.Integer\"/>\n"+
	"			<attribute name=\"minute\" value=\"27\" class=\"java.lang.Integer\"/>\n"+
	"			<attribute name=\"alias\" class=\"java.lang.String\">test task1</attribute>\n"+
	"		</instance>\n"+
	"	</attribute>\n"+
	"</instance>";
	
	@Test
	public void testTriggersCanBePersisted() throws IOException {
		File temp = File.createTempFile("test", "json");
		TimeTrigger timeTrigger = TimeTrigger.initialise(temp.getAbsolutePath());
		timeTrigger.runDaily("test task1", 12, 27, "println(\"Testing\");");
		timeTrigger.runDaily("test task2", 13, 27, "println(\"Testing\");");
		timeTrigger.saveSchedule();
		
		String data = FileHelper.loadFileAsString(temp);
		
		assertEquals("Config mismatch",CONFIG,data);
	}
	
	@Test
	public void testTriggersCanBeInitialisedFromConfig() throws IOException {
		File temp = File.createTempFile("test", "json");
		FileHelper.saveStringToFile(CONFIG, temp);
		
		TimeTrigger timeTrigger = TimeTrigger.initialise(temp.getAbsolutePath());
		
		ScheduledTask task = timeTrigger.getScheduledTask("test task1");
		 assertEquals(Integer.valueOf(12),task.getHour());
		 assertEquals(Integer.valueOf(27),task.getMinute());
	}

}
