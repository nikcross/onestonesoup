package org.one.stone.soup.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHelper {
	
	public static String loadFileAsString(String fileName) throws IOException {
		return loadFileAsString(new File(fileName));
	}
	public static String loadFileAsString(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		StringBuffer data = new StringBuffer();
		byte[] block = new byte[1000];
		try{
			int size = in.read(block);
			while(size!=-1) {
				data.append( new String(block,0,size) );
				size = in.read(block);
			}
		} finally {

			in.close();
		}
		return data.toString();
	}
	public static void saveStringToFile(String data,String fileName) throws IOException {
		saveStringToFile(data,new File(fileName));
	}
	public static void saveStringToFile(String data,File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		
		try{
			out.write(data.getBytes());
			out.flush();
		} finally {
			out.close();
		}
	}
	public static void moveFileToFolder(String file,String folder,boolean createFolder) throws IOException {
		moveFileToFolder(new File(file), new File(folder),createFolder);
	}
	public static void moveFileToFolder(String file,String folder) throws IOException {
		moveFileToFolder(new File(file), new File(folder),false);
	}
	public static void moveFileToFolder(File file,File folder) throws IOException {
		moveFileToFolder(file, folder,false);
	}
	public static void moveFileToFolder(File file,File folder,boolean createFolder) throws IOException {
		if(folder.exists()==false) {
			if(createFolder) {
				boolean result = folder.mkdirs();
				if(result==false) {
					throw new IOException("Failed to create folder "+folder.getAbsolutePath());
				}
			} else {
				throw new IOException("Folder "+folder.getAbsolutePath()+" does not exist");
			}
		}
		File destination = new File(folder.getAbsolutePath()+File.separator+file.getName());
		boolean result = file.renameTo(destination);
		if(result==false) {
			throw new IOException("Failed to move "+file.getAbsolutePath()+" to "+destination.getAbsolutePath());			
		}
	}
}
