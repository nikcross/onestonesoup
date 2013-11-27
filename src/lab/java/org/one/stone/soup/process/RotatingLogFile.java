package org.one.stone.soup.process;

import java.io.File;
import java.io.IOException;

import org.one.stone.soup.core.FileHelper;

public class RotatingLogFile implements LogFile {

	private File fileA;
	private File fileB;

	private File currentFile;
	private long maxSize = 10000000; //10M
	
	public RotatingLogFile(String fileNameA,String fileNameB) {
		fileA = new File(fileNameA);
		fileB = new File(fileNameB);
	}
	
	public void logMessage(String message) {
		message += "\n";
		try {
			FileHelper.appendStringToFile(message, currentFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(currentFile.length()>=maxSize) {
			try {
				switchLogFiles();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void switchLogFiles() throws IOException {
		if(currentFile==fileA) {
			currentFile = fileB;
		} else {
			currentFile = fileA;
		}
		FileHelper.saveStringToFile("", currentFile);
	}
}
