package org.one.stone.soup.javascript.helper;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.javascript.helper.TimeTrigger.ScheduledTask;

public class TimeTriggerTest {

	@Test
	public void testTriggersCanBePersisted() throws IOException {
		File temp = File.createTempFile("test", "json");
		TimeTrigger timeTrigger = TimeTrigger.initialise(temp.getAbsolutePath());
		timeTrigger.runDaily("test task1", 12, 27, "println(\"Testing\");");
		timeTrigger.runDaily("test task2", 13, 27, "println(\"Testing\");");
		timeTrigger.saveSchedule();
		
		String data = FileHelper.loadFileAsString(temp);
		
		assertEquals("{script: \"println(\\\"Testing\\\");\",hour: 12,minute: 27,alias: \"test task\"}",data);
	}
	
	@Test
	public void testTriggersCanBeInitialisedFromConfig() throws IOException {
		File temp = File.createTempFile("test", "json");
		FileHelper.saveStringToFile("{script: \"println(\\\"Testing\\\");\",hour: 12,minute: 27}", temp);
		
		TimeTrigger timeTrigger = TimeTrigger.initialise(temp.getAbsolutePath());
		
		ScheduledTask task = timeTrigger.getScheduledTask("test task");
		 assertEquals(Integer.valueOf(12),task.getHour());
		 assertEquals(Integer.valueOf(27),task.getMinute());
	}

}
