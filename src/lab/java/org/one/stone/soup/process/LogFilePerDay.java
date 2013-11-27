package org.one.stone.soup.process;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.one.stone.soup.core.FileHelper;

public class LogFilePerDay implements LogFile {

	private String filePath;
	private String fileName;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy");
	
	public LogFilePerDay(String fileName) {
		this.filePath = new File(fileName).getParent();
		this.fileName = new File(fileName).getName();
	}
	
	public void logMessage(String message) {
		File file = new File(filePath+"/"+getDate()+fileName);
		
		message += "\n";
		try {
			FileHelper.appendStringToFile(message, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getDate() {
		return dateFormat.format(new Date());
	}
}
