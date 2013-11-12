package org.one.stone.soup.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

public class FileHelper {
	
	public static String loadFileAsString(String fileName) throws IOException {
		return loadFileAsString(new File(fileName));
	}
	public static String loadFileAsString(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		return loadFileAsString(in);
	}
	
	public static String loadFileAsString(InputStream in) throws IOException {	
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
	public static void appendStringToFile(String data,File file) throws IOException {
		OutputStream out = new FileOutputStream(file,true);
		
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
	public static void copyFileToFile(File fromFile,File toFile) throws IOException {
		InputStream in = new FileInputStream(fromFile);
		FileOutputStream oStream = new FileOutputStream(toFile);

		byte[] buffer = new byte[1000];
		int sizeRead = in.read(buffer);
		while(sizeRead>0)
		{
			oStream.write(buffer,0,sizeRead);
			oStream.flush();
			sizeRead = in.read(buffer);
		}
		in.close();
		oStream.close();
	}
	
	public static void copyInputStreamToFile(InputStream fromInputStream,File toFile) throws IOException {
		FileOutputStream oStream = new FileOutputStream(toFile);

		byte[] buffer = new byte[1000];
		int sizeRead = fromInputStream.read(buffer);
		while(sizeRead>0)
		{
			oStream.write(buffer,0,sizeRead);
			oStream.flush();
			sizeRead = fromInputStream.read(buffer);
		}
		fromInputStream.close();
		oStream.close();
	}
	public static int countLines(File file) throws IOException {
		String data = FileHelper.loadFileAsString(file);
		String[] lines = data.split("\n");
		
		return lines.length;
	}
	
	public String getExtension(String fileName) {
		if(fileName.indexOf(".")!=-1) {
			return fileName.substring(fileName.lastIndexOf("."));
		} else {
			return "";
		}
	}
	
	public static String generateMD5Checksum(File file) {
		try{
			byte[] buffer = new byte[1000];
			MessageDigest MD5 = MessageDigest.getInstance("MD5");
			InputStream fromInputStream = new FileInputStream(file);
			int sizeRead = fromInputStream.read(buffer);
			while(sizeRead>0)
			{
				MD5.update(buffer,0,sizeRead);
				sizeRead = fromInputStream.read(buffer);
			}
			fromInputStream.close();
			byte[] hash = MD5.digest();
			
			return DatatypeConverter.printHexBinary(hash);
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	public static void saveStringToOutputStream(String data,
			OutputStream out) throws IOException {
		try{
			out.write(data.getBytes());
			out.flush();
		} finally {
			out.close();
		}
	}
}
