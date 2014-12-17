package org.onestonesoup.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileWatch {
	
	private class FileRecord {
		File file;
		long lastModified;
		List<FileWatcher> watchers = new ArrayList<FileWatcher>();
	}
	private Map<String,FileRecord> filesWatched = new HashMap<String,FileRecord>();
	
	public void addFileWatcher(String fileName,FileWatcher watcher) {
		
	}
}
