package org.one.stone.soup.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirectoryHelper {

	public static List<File> findFiles(String directoryName,String regex) {
		return findFiles(directoryName, regex, false);
	}
	
	public static List<File> findFilesWithExtension(String directoryName,String extension,boolean recursiveSearch) {
		return findFiles(directoryName,".*\\."+extension,recursiveSearch);
	}
	public static List<File> findFiles(String directoryName,String regex,boolean recursiveSearch) {
		File directory = new File(directoryName);
		List<File> matches = new ArrayList<File>();
		
		for(File file: directory.listFiles()) {
			if(file.getName().matches(regex)) {
				matches.add(file);
			} else if (recursiveSearch==true && file.isDirectory()) {
				matches.addAll( findFiles(file.getAbsolutePath(),regex,true) );
			}
		}
		
		return matches;
	}
}
