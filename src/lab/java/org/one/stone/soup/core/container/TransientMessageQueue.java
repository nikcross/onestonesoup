package org.one.stone.soup.core.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 
 * A queue of time stamped text based messages that are deleted after a set period.
 * Subscribers can ask for any messages that have arrived after a given time.
 * The subscriber keeps track of the last time they asked.
 * Transient Queue can be used as pseudo push service where the subscriber pulls
 * the data that would have been pushed over a given period, removing the need for
 * a constant connection and a push stream.
 * E.g. could be used as a web chat service.
 *
 */
public class TransientMessageQueue {
	private static final long DEFAULT_LIFETIME = 60000;
	private static final long PRUNE_TIME = 1000;
	private long lifeTime = DEFAULT_LIFETIME;
	private long latestMessageTime = 0;
	private Timer timer;
	
	private List<QueueEntry> entries = new ArrayList<QueueEntry>();
	private class QueueEntry {
		QueueEntry(String message) {
			this.message = message;
			this.timeStamp = System.currentTimeMillis();
			latestMessageTime = this.timeStamp;
		}
		long timeStamp;
		String message;
		boolean toDelete = false;
	}
	
	private class QueueCleaner extends TimerTask {
		@Override
		public void run() {
			removeOldMessages();
		}
	}
	
	public TransientMessageQueue() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new QueueCleaner(), PRUNE_TIME, PRUNE_TIME);
	}
	
	public long getTime() {
		return System.currentTimeMillis();
	}
	
	public int getMessageCount() {
		return entries.size();
	}
	
	public long getMessageLifeTime() {
		return lifeTime;
	}
	
	public void setMessageLifeTime(long lifeTime) {
		this.lifeTime =lifeTime;  
	}
	
	public void post(String message) {
		entries.add( new QueueEntry(message) );
	}
	
	public boolean hasMessagesSince(long time) {
		if(latestMessageTime<time) {
			return false;
		} else {
			return true;
		}
	}
	
	public List<String> getMessagesSince(long earliestTime) {
		List<String> messages = new ArrayList<String>();
		if(hasMessagesSince(earliestTime)==false) {
			return messages;
		}
		for(QueueEntry entry: entries) {
			if(entry.timeStamp>earliestTime) {
				messages.add(entry.message);
			}
		}
		return messages;
	}
	
	private void removeOldMessages() {
		long deleteTime = System.currentTimeMillis()-lifeTime;
		for(QueueEntry entry: entries) {
			if(entry.timeStamp<deleteTime) {
				entry.toDelete=true;
			}
		}
		for(QueueEntry entry: entries) {
			if(entry.toDelete==true) {
				entries.remove(entry);
			}
		}
	}
}
