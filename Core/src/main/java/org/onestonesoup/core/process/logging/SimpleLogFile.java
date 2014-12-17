package org.onestonesoup.core.process.logging;

import java.io.File;
import java.io.IOException;

import org.onestonesoup.core.FileHelper;

public class SimpleLogFile implements LogFile {

	private File file;
	
	public SimpleLogFile(String fileName) {
		file = new File(fileName);
	}
	
	public void logMessage(String message) {
		message += "\n";
		try {
			FileHelper.appendStringToFile(message, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
