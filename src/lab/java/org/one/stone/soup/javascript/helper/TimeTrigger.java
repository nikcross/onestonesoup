package org.one.stone.soup.javascript.helper;

import java.util.HashMap;
import java.util.Map;

import org.one.stone.soup.core.javascript.JSONHelper;

public class TimeTrigger implements Runnable {

	private static TimeTrigger timeTrigger;
	private String fileName;
	
	public static TimeTrigger initialise(String fileName) {
		TimeTrigger timeTrigger = getInstance();
		timeTrigger.fileName = fileName;
		timeTrigger.importSchedule();
		return timeTrigger;
	}
	
	public static void saveSchedule() {
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
	
	public static void runDaily(int hour, int minutes,String script) {
		//TODO
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
	private class ScheduledTask {
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
		//TODO
	}
	
	@Override
	public void finalize() {
		exportSchedule();
	}
	
	private void exportSchedule() {
		if(fileName==null) {
			return;
		}
		//TODO
		for(String key: schedule.keySet()) {
			ScheduledTask task = schedule.get(key);
			JSONHelper.toJSON(task);
		}
	}
}
