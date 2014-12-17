package org.onestonesoup.file.analysis;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.onestonesoup.core.FileHelper;
import org.onestonesoup.core.process.CommandLineTool;
import org.onestonesoup.core.process.logging.SimpleLogFile;

public class FileDuplicateAnalysis extends CommandLineTool {

	private Map<String,File> map;
	private SimpleLogFile logFile;
	private long bytesRead;
	private long duplicateBytesRead;
	private long directoriesProcessed;
	private long filesProcessed;
	private long duplicateFilesRead;
	
	public static final void main(String[] args) {
		new FileDuplicateAnalysis(args);
	}	
	
	public FileDuplicateAnalysis(String[] args) {
		super(args);
	}
	
	@Override
	public int getMinimumArguments() {
		return 2;
	}

	@Override
	public int getMaximumArguments() {
		return 2;
	}

	@Override
	public String getUsage() {
		return "[-D = delete duplicates] [-R = recursive] <a root directory to search> <a log file for results>";
	}

	@Override
	public void process() {
		bytesRead=0;
		duplicateBytesRead=0;
		directoriesProcessed=0;
		filesProcessed=0;
		duplicateFilesRead=0;
		
		map = new HashMap<String,File>();
		File root = new File( getParameter(0) );
		logFile = new SimpleLogFile( getParameter(1) );
		processDirectory(root);
		
		logFile.logMessage("Processed "+filesProcessed+" files in "+directoriesProcessed+" directories. Duplicates found: "+duplicateFilesRead+". "+bytesRead+" bytes read of which "+duplicateBytesRead+" was duplicate data.");
	}
	
	private void processDirectory(File directory) {
		directoriesProcessed++;
		System.out.println("Checking "+directory.getAbsolutePath());
		File[] files = directory.listFiles();
				
		if(files==null) {
			System.out.println("No Files Here");
			return;
		}
		
		for(File file: files) {
			if(filesProcessed%100==0) {
				System.out.println("Processed "+filesProcessed+" files in "+directoriesProcessed+" directories. Duplicates found: "+duplicateFilesRead+". "+bytesRead+" bytes read of which "+duplicateBytesRead+" was duplicate data.");
			}
			
			if(file.isDirectory()) {
				if(hasOption("R")) {
					//recurse
					processDirectory(file);
				}
				continue;
			}
			
			String check = null;
			if(file.length()==0) {
				check="[empty file]";
			} else {
				check=FileHelper.generateMD5Checksum(file);
			}
			
			if(map.get(check)!=null) {
				logFile.logMessage("Found match "+file.getAbsolutePath()+" = "+map.get(check).getAbsolutePath()+" MD5:"+check);
				duplicateBytesRead+=file.length();
				duplicateFilesRead++;
				
				if(hasOption("D")) {
					boolean result = file.delete();
					if(result==false) {
						logFile.logMessage("FAILED to delete "+file.getAbsolutePath());
					} else {
						logFile.logMessage("DELETED "+file.getAbsolutePath());
					}
				}
			} else {
				map.put(check, file);
			}
			filesProcessed++;
			bytesRead+=file.length();
		}
	}	
}
