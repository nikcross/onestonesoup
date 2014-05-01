package org.one.stone.soup.file.crawler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirectoryCrawler {

	public interface DirectoryListener {
		public void directoryFound(File directory);
	}
	
	public interface FileListener {
		public void fileFound(File file);
	}
	
	private List<FileListener> fileListeners = new ArrayList<FileListener>();
	private List<DirectoryListener> directoryListeners = new ArrayList<DirectoryListener>();
	
	public void addFileListener(FileListener fileListener) {
		fileListeners.add(fileListener);
	}
	
	public void addDirectoryListener(DirectoryListener directoryListener) {
		directoryListeners.add(directoryListener);
	}
	
	public void crawl(File directory) {
		if(directory.isDirectory()) {
			for(DirectoryListener directoryListener: directoryListeners) {
				directoryListener.directoryFound(directory);
			}
			processDirectory(directory);
		} else {
			for(FileListener fileListener: fileListeners) {
				fileListener.fileFound(directory);
			}
			processFile(directory);
			return;
		}
		
		for(File file: directory.listFiles()) {
			crawl(file);
		}
	}
	
	public void processDirectory(File Directory) {}
	public void processFile(File file) {}
}
