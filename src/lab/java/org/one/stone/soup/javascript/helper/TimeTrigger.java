package org.one.stone.soup.javascript.helper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.core.javascript.JSONHelper;

public class TimeTrigger implements Runnable {

	private static TimeTrigger timeTrigger;
	private String fileName = "user/admin/TimeTrigger.config.JSON";
	
	public static TimeTrigger initialise(String fileName) {
		TimeTrigger timeTrigger = getInstance();
		timeTrigger.fileName = fileName;
		timeTrigger.importSchedule();
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
	
	public static void runFrequently(long milliSeconds,String script) {
		//TODO
	}
	
	public static void runHourly(int minutes,String script) {
		//TODO
	}
	
	public static void runDaily(String alias,int hour, int minutes,String script) {
				
		ScheduledTask task = getInstance().getNewScheduledTask();
		task.hour = hour;
		task.minute = minutes;
		task.script = script;
		task.alias = alias;
		
		getInstance().schedule.put(alias, task);
	}
	
	private ScheduledTask getNewScheduledTask() {
		return new ScheduledTask();
	}

	public static void runMonthly(int day, int hour,String script) {
		//TODO
	}
	
	//Sunday is 0 ?
	public static void runWeekly(int day, int hour,String script) {
		//TODO
	}
	
	public static void runYearly(int day, int hour,String script) {
		//TODO
	}

	private TimeTrigger() {
		new Thread(this,"TimeTrigger alarm clock").start();
	}
	
	private void runEvery(Integer year,Integer month, Integer day, Integer dayOfWeek, Integer hour, Integer minute, Integer second,String script) {
		//TODO
	}
	
	private boolean running = false;
	public class ScheduledTask {
		public String getAlias() {
			return alias;
		}
		public void setAlias(String alias) {
			this.alias = alias;
		}
		public Integer getYear() {
			return year;
		}
		public void setYear(Integer year) {
			this.year = year;
		}
		public Integer getMonth() {
			return month;
		}
		public void setMonth(Integer month) {
			this.month = month;
		}
		public Integer getDay() {
			return day;
		}
		public void setDay(Integer day) {
			this.day = day;
		}
		public Integer getDayOfWeek() {
			return dayOfWeek;
		}
		public void setDayOfWeek(Integer dayOfWeek) {
			this.dayOfWeek = dayOfWeek;
		}
		public Integer getHour() {
			return hour;
		}
		public void setHour(Integer hour) {
			this.hour = hour;
		}
		public Integer getMinute() {
			return minute;
		}
		public void setMinute(Integer minute) {
			this.minute = minute;
		}
		public Integer getSecond() {
			return second;
		}
		public void setSecond(Integer second) {
			this.second = second;
		}
		public String getScript() {
			return script;
		}
		public void setScript(String script) {
			this.script = script;
		}
		private String alias;
		private Integer year;
		private Integer month;
		private Integer day;
		private Integer dayOfWeek;
		private Integer hour;
		private Integer minute;
		private Integer second;
		private String script;
	}
	
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
						//TODO
						//JS.getInstance().runAsync( task.script,"Scheduled task "+task.alias );
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
		//TODO
		return false;
	}
	
	private void importSchedule() {
		if(fileName==null) {
			return;
		}
		File config = new File(fileName);
		if(config.exists()==false) {
			return;
		}
		//TODO
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
		File config = new File(fileName);
		if(config.getParentFile().exists()==false) {
			config.getParentFile().mkdirs();
		}
		
		StringBuffer configData = new StringBuffer();
		for(String key: schedule.keySet()) {
			ScheduledTask task = schedule.get(key);
			configData.append( JSONHelper.toJSON(task) );
		}
		
		FileHelper.saveStringToFile(configData.toString(), config);
	}
}
