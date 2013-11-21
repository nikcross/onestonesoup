package org.one.stone.soup.sds.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.one.stone.soup.core.FileHelper;

public class TextFileService implements SDSService {

	private String root = null;
	public void setRoot(String root) throws Exception {
		if(this.root==null) {
			this.root = root;
		} else {
			throw new Exception("Cannot reset root once set.");
		}
	}
	
	public String getCurrentTime() {
		return ""+System.currentTimeMillis();
	}
	
	public String getLastModified(String fileName) {
		return ""+getFile(fileName).lastModified();
	}
	
	public String getLength(String fileName) {
		return ""+getFile(fileName).length();
	}
	
	public boolean save(String fileName,String data) {
		try {
			FileHelper.saveStringToFile(data, getFile(fileName));
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public String load(String fileName) {
		try {
			return FileHelper.loadFileAsString(getFile(fileName));
		} catch (IOException e) {
			return null;
		}
	}
	
	public boolean delete(String fileName) {
		return getFile(fileName).delete();
	}
	
	public List<String> listFiles(String directory) {
		List<String> files = new ArrayList<String>();
		for(File file: getFile(directory).listFiles()) {
			if(file.isDirectory()) {
				continue;
			}
			files.add(directory+"/"+file.getName());
		}
		return files;
	}
	
	public List<String> listDirectories(String directory) {
		List<String> files = new ArrayList<String>();
		for(File file: getFile(directory).listFiles()) {
			if(file.isDirectory()==false) {
				continue;
			}
			files.add(directory+"/"+file.getName());
		}
		return files;		
	}
	
	private File getFile(String fileName) {
		return new File( root+"/"+fileName );
	}
}
