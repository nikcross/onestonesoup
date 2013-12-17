package org.one.stone.soup.javascript.helper;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.core.constants.TimeConstants;
import org.one.stone.soup.core.data.JavaTree;
import org.one.stone.soup.core.data.XmlHelper;
import org.one.stone.soup.core.data.XmlHelper.XmlParseException;
import org.one.stone.soup.javascript.JS;

public class TimeTrigger implements Runnable {

	private static TimeTrigger timeTrigger;
	private String fileName = "user/admin/TimeTrigger.config.xml";
	
	public static TimeTrigger initialise(String fileName) {
		TimeTrigger timeTrigger = getInstance();
		timeTrigger.fileName = fileName;
		try {
			timeTrigger.importSchedule();
		} catch (Throwable e) {
			System.out.println("TimeTrigger config not loaded from "+fileName);
		}
		return timeTrigger;
	}
	
	public ScheduledTask getScheduledTask(String alias) {
		return schedule.get(alias);
	}
	public static void saveSchedule() throws IOException {
		getInstance().exportSchedule();
	}
	
	public static TimeTrigger getInstance() {
		if(timeTrigger==null) {
			timeTrigger = new TimeTrigger();
		}
		return timeTrigger;
	}
	
	public static void runFrequently(String alias,int milliSeconds,String script) {
		ScheduledTask task = getInstance().getNewScheduledTask();
		task.setAlias(alias);
		task.setMilliseconds(milliSeconds);
		getInstance().schedule.put(alias, task);
	}
	
	public static void runHourly(String alias,int minutes,String script) {
		ScheduledTask task = getInstance().getNewScheduledTask();
		task.setAlias(alias);
		task.setMinute(minutes);
		getInstance().schedule.put(alias, task);
	}
	
	public static void runDaily(String alias,int hour, int minutes,String script) {
		ScheduledTask task = getInstance().getNewScheduledTask();
		task.setHour(hour);
		task.setMinute(minutes);
		task.setScript(script);
		task.setAlias(alias);
		getInstance().schedule.put(alias, task);
	}
	
	private ScheduledTask getNewScheduledTask() {
		return new ScheduledTask();
	}

	public static void runMonthly(String alias,int day, int hour,String script) {
		ScheduledTask task = getInstance().getNewScheduledTask();
		task.setDay(day);
		task.setHour(hour);
		task.setScript(script);
		task.setAlias(alias);
		getInstance().schedule.put(alias, task);
	}
	
	//Sunday is 0 ?
	public static void runWeekly(String alias,int day, int hour,String script) {
		ScheduledTask task = getInstance().getNewScheduledTask();
		task.setHour(hour);
		task.setDay(day);
		task.setScript(script);
		task.setAlias(alias);
		getInstance().schedule.put(alias, task);
	}
	
	public static void runYearly(String alias,int month,int day, int hour,String script) {
		ScheduledTask task = getInstance().getNewScheduledTask();
		task.setMonth(month);
		task.setHour(hour);
		task.setDay(day);
		task.setScript(script);
		task.setAlias(alias);
		getInstance().schedule.put(alias, task);
	}

	public static void removeTask(String alias) {
		getInstance().schedule.remove(alias);
	}
	
	private TimeTrigger() {
		new Thread(this,"TimeTrigger alarm clock").start();
	}
	
	private boolean running = false;
	private Map<String,ScheduledTask> schedule = new HashMap<String,ScheduledTask>();
	
	@Override
	public void run() {
		if(running) {
			return;
		}
		running = true;
		while(running) {
			try{
				for(String key: schedule.keySet()) {
					ScheduledTask task = schedule.get(key);
					long now = System.currentTimeMillis();
					if(timeToRunScheduleTask(task,now)) {
						JS.getInstance().runAsync(task.getScript());
					}
				}
				Thread.sleep(100);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		running = false;
	}
	
	private boolean timeToRunScheduleTask(ScheduledTask task,long time) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(time);
		
		boolean timeToRun = true;
		if(task.getMilliseconds()!=null) {
			if(task.getMilliseconds().intValue()!=time%TimeConstants.SECOND) {
				timeToRun = false;
			}
		}
		if(task.getSecond()!=null) {
			if(task.getSecond().intValue()!=now.get(Calendar.SECOND)) {
				timeToRun = false;
			}
		}
		if(task.getMinute()!=null) {
			if(task.getMinute().intValue()!=now.get(Calendar.MINUTE)) {
				timeToRun = false;
			}
		}
		if(task.getHour()!=null) {
			if(task.getHour().intValue()!=now.get(Calendar.HOUR)) {
				timeToRun = false;
			}
		}
		if(task.getDay()!=null) {
			if(task.getDay().intValue()!=now.get(Calendar.DAY_OF_MONTH)) {
				timeToRun = false;
			}
		}
		if(task.getMonth()!=null) {
			if(task.getMonth()!=now.get(Calendar.MONTH)) {
				timeToRun = false;
			}
		}
		if(task.getDayOfWeek()!=null) {
			if(task.getDayOfWeek()!=now.get(Calendar.DAY_OF_WEEK)) {
				timeToRun = false;
			}			
		}
		if(task.getYear()!=null) {
			if(task.getYear()!=now.get(Calendar.YEAR)) {
				timeToRun = false;
			}			
		}
		
		return timeToRun;
	}
	
	private void importSchedule() throws IOException, XmlParseException {
		if(fileName==null) {
			return;
		}
		File configFile = new File(fileName);
		if(configFile.exists()==false) {
			return;
		}
		//Object object = JSONHelper.fromJSON( FileHelper.loadFileAsString(fileName) );
		TimeTriggerConfiguration config = (TimeTriggerConfiguration)JavaTree.toObject( XmlHelper.parseElement(FileHelper.loadFileAsString(fileName)) );
		for(ScheduledTask task: config.getScheduledTasks()) {
			schedule.put(task.getAlias(), task);
		}
		System.out.println("Loaded TimeTrigger config "+configFile);
	}
	
	@Override
	public void finalize() {
		try {
			exportSchedule();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void exportSchedule() throws IOException {
		if(fileName==null) {
			return;
		}
		File configFile = new File(fileName);
		if(configFile.getParentFile().exists()==false) {
			configFile.getParentFile().mkdirs();
		}
		
		ScheduledTask[] tasks = schedule.values().toArray(new ScheduledTask[]{});
		TimeTriggerConfiguration config = new TimeTriggerConfiguration();
		config.setScheduledTasks(tasks);
		
		String configData = XmlHelper.toXml( JavaTree.toEntityTree(config) );
		FileHelper.saveStringToFile(configData, configFile);
		
		System.out.println("Saved TimeTrigger config to "+configFile.getAbsolutePath());
	}
}
