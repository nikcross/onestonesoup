package org.one.stone.soup.file.crawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.core.StringHelper;
import org.one.stone.soup.core.constants.TimeConstants;

public class DirectorySynchronizer extends DirectoryCrawler implements Runnable{

	private File sourceDirectoryA;
	private File sourceDirectoryB;
	private boolean deleteFiles = false;
	private long timeBetweenRuns = TimeConstants.SECOND*15;
	private long timeBetweenFiles = 10;
	private boolean running=false;
	private String primaryLogFile;
	private Map<String,String> directoryLogs;
	private String state;
	
	
	public void start() {
		new Thread(this,"DirectorySynchronizer").start();
	}
	
	public void stop() {
		running = false;
	}
	
	public boolean willDeleteFiles() {
		return deleteFiles;
	}
	
	public void setDeleteFiles(boolean willDeleteFiles) {
		deleteFiles = willDeleteFiles;
	}
	
	public void run() {
		if(running) {
			return;
		}
		running = true;
		setState("Started");
		
		while(running) {

			setState("Loading directory primary log");
			loadPrimaryLogFile();
			
			this.crawl(sourceDirectoryA);
			this.crawl(sourceDirectoryB);
			
			savePrimaryLogFile();
			

			setState("Sleeping");
			try{ Thread.sleep(timeBetweenRuns); }catch(Exception e){}
		}
		

		setState("Stopped");
	}
	
	private void loadPrimaryLogFile() {
		String data;
		try {
			data = FileHelper.loadFileAsString(primaryLogFile+"/dir-log.txt");
			String[] lines = data.split("\n");
			directoryLogs = new HashMap<String,String>();
			for(String line: lines) {
				if(line.length()==0) {
					continue;
				}
				String[] part = line.split(",");
				
				directoryLogs.put(part[0],part[1]);
			}
		} catch (IOException e) {
			//e.printStackTrace();
			directoryLogs = new HashMap<String,String>();
			savePrimaryLogFile();
			System.out.println("New primary log created");
		}
	}

	private void savePrimaryLogFile() {
		StringBuilder data = new StringBuilder();
		for(String key: directoryLogs.keySet()) {
			String value = directoryLogs.get(key);
			data.append(key+","+value+"\n");
		}
		
		try {
			FileHelper.saveStringToFile(data.toString(),new File(primaryLogFile+"/dir-log.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void processDirectory(File directory) {
		setState("Processing directory "+directory.getAbsolutePath());		
		
		String directoryName = getGenericName(directory);
		String directoryLogName = directoryLogs.get(directoryName);
		if(directoryLogName==null) {
			//create new directory log
			directoryLogName = primaryLogFile+"/log"+System.currentTimeMillis()+".txt";
			directoryLogs.put(directoryName,directoryLogName);
			try {
				FileHelper.saveStringToFile("", new File(directoryLogName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Map<String,LogEntry> log = loadDirectoryLogFile( directoryLogName );
		boolean hasChanges = false;
		
		for(File file: directory.listFiles()) {
			if(file.isDirectory()) {
				continue;
			}
			LogEntry logEntry = log.get( getGenericName(file) );
			if(logEntry==null) { // New file
				//sync file for A and B
				logEntry = syncFiles(file);
				//add entry to log
				log.put(getGenericName(file), logEntry);
				hasChanges = true;
			} else {
				LogEntry currentLogEntry = buildLogEntry(file);
				if(currentLogEntry.hash.equals(logEntry.hash)) {
					// do nothing
				} else {
					//sync file for A and B
					logEntry = syncFiles(file);
					//replace log entry
					log.put(getGenericName(file), logEntry);
					hasChanges = true;
				}
			}
		}
		
		//Check for deleted
		List<String> keySet = new ArrayList<String>();
		keySet.addAll(log.keySet());
		for(String key: keySet) {
			//LogEntry logEntry = log.get(key);
			
			String testName = new File(key).getName();
			File testFile = new File(directory.getAbsolutePath()+"/"+testName);
			if(testFile.exists()==false) {// deleted
				testFile = getOtherFile(testFile);
				if(testFile.exists()==true) {
					if(deleteFiles==true) {
						System.out.println("Deleting "+testFile.getAbsolutePath());
						testFile.delete();
					} else {
						System.out.println("NOT Deleting "+testFile.getAbsolutePath());
					}
					log.remove(key);
					hasChanges=true;
				}
			}
		}
		
		if(hasChanges==true) {
			saveDirectoryLogFile(directoryLogName, log);
		}
		
		try{ Thread.sleep(timeBetweenFiles); } catch(Exception e){};
	}
	
	private Map<String, LogEntry> loadDirectoryLogFile(String directoryLogName) {
		String data;
		try {
			data = FileHelper.loadFileAsString(directoryLogName);
			String[] lines = data.split("\n");
			Map<String,LogEntry> log = new HashMap<String,LogEntry>();
			
			for(String line: lines) {
				if(line.length()==0) {
					continue;
				}
				LogEntry entry = parseLogEntry(line);
				log.put(entry.fileName,entry);
			}
			
			return log;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void saveDirectoryLogFile(String directoryLogName,Map<String, LogEntry> log) {
		try {
			FileHelper.saveStringToFile("", directoryLogName); // clear the log
			for(String fileName: log.keySet()) {
				LogEntry logEntry = log.get(fileName);
				FileHelper.appendStringToFile(logEntry.toString()+"\n", new File(directoryLogName));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	@Override
	public void processFile(File file) {
		//No Action
	}

	private LogEntry syncFiles(File file) {
		File otherFile = getOtherFile(file);
		File bestFile = null;
		
		if(file.lastModified()>otherFile.lastModified()) {
			try {
				otherFile.getParentFile().mkdirs();
				FileHelper.copyFileToFile(file, otherFile);
				otherFile.setLastModified(file.lastModified());
				bestFile = file;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				file.getParentFile().mkdirs();
				FileHelper.copyFileToFile(otherFile,file);
				file.setLastModified(otherFile.lastModified());
				bestFile = otherFile;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return buildLogEntry(bestFile);
	}
	
	private File getOtherFile(File directory) {
		if(directory.getAbsolutePath().startsWith(sourceDirectoryA.getAbsolutePath())) {
			return new File(
					sourceDirectoryB.getAbsolutePath()+
					directory.getAbsolutePath().substring(
							sourceDirectoryA.getAbsolutePath().length()
					)
				);
		} else {
			return new File(
					sourceDirectoryA.getAbsolutePath()+
					directory.getAbsolutePath().substring(
							sourceDirectoryB.getAbsolutePath().length()
					)
				);
		}
	}
	
	private String getGenericName(File directory) {
		if(directory.getAbsolutePath().startsWith(sourceDirectoryA.getAbsolutePath())) {
			return StringHelper.after(directory.getAbsolutePath(), sourceDirectoryA.getAbsolutePath());
		} else  if(directory.getAbsolutePath().startsWith(sourceDirectoryB.getAbsolutePath())) {
			return StringHelper.after(directory.getAbsolutePath(), sourceDirectoryB.getAbsolutePath());
		} else {
			return null;
		}
	}
	
	private LogEntry buildLogEntry(File file) {
		LogEntry logEntry = new LogEntry();
		logEntry.file = file;
		logEntry.fileName = getGenericName(file);
		logEntry.systemTime = System.currentTimeMillis();
		logEntry.length = file.length();
		logEntry.lastModified = file.lastModified();
		logEntry.hash = FileHelper.generateMD5Checksum(file);
		if(file.isDirectory()) {
			logEntry.type = "D";
		} else {
			logEntry.type = "F";
		}
		
		return logEntry;
	}
	
	private class LogEntry {
		File file;
		String fileName;
		long systemTime;
		long length;
		long lastModified;
		String hash;
		String type;
		
		public String toString() {
			StringBuilder line = new StringBuilder();
			line.append(fileName);
			line.append(",");
			line.append(systemTime);
			line.append(",");
			line.append(length);
			line.append(",");
			line.append(lastModified);
			line.append(",");
			line.append(hash);
			line.append(",");
			line.append(type);
			
			return line.toString();
		}
	}
	
	private LogEntry parseLogEntry(String line) {
		String[] part = line.split(",");
		
		LogEntry logEntry = new LogEntry();
		logEntry.fileName = part[0];
		logEntry.systemTime = Long.parseLong(part[1]);
		logEntry.length = Long.parseLong(part[2]);
		logEntry.lastModified = Long.parseLong(part[3]);
		logEntry.hash = part[4];
		logEntry.type = part[5];
		
		return logEntry;
	}

	public void setLogPath(String parameter) {
		primaryLogFile = parameter;
	}

	public void setDirectoryA(String parameter) {
		this.sourceDirectoryA = new File(parameter);
	}
	
	public void setDirectoryB(String parameter) {
		this.sourceDirectoryB = new File(parameter);
	}

	public String getState() {
		return state;
	}
	
	private void setState(String state) {
		this.state = state;
	}
}
