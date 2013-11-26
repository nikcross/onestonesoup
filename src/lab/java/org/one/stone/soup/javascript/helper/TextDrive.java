package org.one.stone.soup.javascript.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.sds.SDSService;

public class TextDrive implements FileReadInterface, FileWriteInterface {

	private String root = null;
	/* (non-Javadoc)
	 * @see org.one.stone.soup.javascript.helper.FileReadInterface#setRoot(java.lang.String)
	 */
	@Override
	public void setRoot(String root) throws Exception {
		if(this.root==null) {
			this.root = root;
		} else {
			throw new Exception("Cannot reset root once set.");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.one.stone.soup.javascript.helper.FileReadInterface#getCurrentTime()
	 */
	@Override
	public String getCurrentTime() {
		return ""+System.currentTimeMillis();
	}
	
	/* (non-Javadoc)
	 * @see org.one.stone.soup.javascript.helper.FileReadInterface#getLastModified(java.lang.String)
	 */
	@Override
	public String getLastModified(String fileName) {
		return ""+getFile(fileName).lastModified();
	}
	
	/* (non-Javadoc)
	 * @see org.one.stone.soup.javascript.helper.FileReadInterface#getLength(java.lang.String)
	 */
	@Override
	public String getLength(String fileName) {
		return ""+getFile(fileName).length();
	}
	
	/* (non-Javadoc)
	 * @see org.one.stone.soup.javascript.helper.FileWriteInterface#save(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean save(String fileName,String data) {
		try {
			FileHelper.saveStringToFile(data, getFile(fileName));
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.one.stone.soup.javascript.helper.FileReadInterface#load(java.lang.String)
	 */
	@Override
	public String load(String fileName) {
		try {
			return FileHelper.loadFileAsString(getFile(fileName));
		} catch (IOException e) {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.one.stone.soup.javascript.helper.FileWriteInterface#delete(java.lang.String)
	 */
	@Override
	public boolean delete(String fileName) {
		return getFile(fileName).delete();
	}
	
	private File getFile(String fileName) {
		return new File( root+"/"+fileName );
	}
	
	/* (non-Javadoc)
	 * @see org.one.stone.soup.javascript.helper.FileReadInterface#listFiles(java.lang.String)
	 */
	@Override
	public String[] listFiles(String directory) {
		File dir = getFile(directory);
		List<String> files = new ArrayList<String>();
		for(File file: dir.listFiles()) {
			if(file.isDirectory()) {
				continue;
			}
			files.add(file.getName());
		}
		
		return files.toArray(new String[]{});
	}
	
	/* (non-Javadoc)
	 * @see org.one.stone.soup.javascript.helper.FileReadInterface#listDirectories(java.lang.String)
	 */
	@Override
	public String[] listDirectories(String directory) {
		File dir = getFile(directory);
		List<String> files = new ArrayList<String>();
		for(File file: dir.listFiles()) {
			if(file.isDirectory()==false) {
				continue;
			}
			files.add(file.getName());
		}
		
		return files.toArray(new String[]{});
	}
	
	/* (non-Javadoc)
	 * @see org.one.stone.soup.javascript.helper.FileWriteInterface#createDirectory(java.lang.String)
	 */
	@Override
	public boolean createDirectory(String directory) {
		File dir = getFile(directory);
		return dir.mkdirs();
	}
}
