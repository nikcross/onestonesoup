package org.onestonesoup.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

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
		file.getParentFile().mkdirs();
		OutputStream out = new FileOutputStream(file);
		
		try{
			out.write(data.getBytes());
			out.flush();
		} finally {
			out.close();
		}
	}
	public static void appendStringToFile(String data,String fileName) throws IOException {
		appendStringToFile(data, new File(fileName));
	}
	
	public static void appendStringToFile(String data,File file) throws IOException {
		file.getParentFile().mkdirs();
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
	public static int copyInputStreamToOutputStream(InputStream fromInputStream,OutputStream oStream) throws IOException {
		return copyInputStreamToOutputStream(fromInputStream, oStream, true);
	}
	public static int copyInputStreamToOutputStream(InputStream fromInputStream,OutputStream oStream,boolean closeFromInputStream) throws IOException {
		byte[] buffer = new byte[1000];
		int sizeRead = fromInputStream.read(buffer);
		while(sizeRead>0)
		{
			oStream.write(buffer,0,sizeRead);
			oStream.flush();
			sizeRead = fromInputStream.read(buffer);
		}
		if(closeFromInputStream) {
			fromInputStream.close();
		}
		oStream.close();
		return sizeRead;
	}	
	public static int countLines(File file) throws IOException {
		String data = FileHelper.loadFileAsString(file);
		String[] lines = data.split("\n");
		
		return lines.length;
	}
	
	public static String getExtension(String fileName) {
		if(fileName.indexOf(".")!=-1) {
			return fileName.substring(fileName.lastIndexOf(".")+1);
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

			return printHexBinary(hash);
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	public static String printHexBinary(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static boolean isSameFile(File fileA, File fileB) {
		if(fileA.length()!=fileB.length()) {
			return false;
		}
		if(fileA.length()==0) {
			return true;
		}
		
		if( generateMD5Checksum(fileA).equals(generateMD5Checksum(fileB)) ) {
			return true;
		} else {
			return false;
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
	public static int copyFileToOutputStream(String fileName,
			OutputStream out) throws IOException {
		InputStream in = new FileInputStream(fileName);

		byte[] buffer = new byte[1000];
		int sizeRead = in.read(buffer);
		while(sizeRead>0)
		{
			out.write(buffer,0,sizeRead);
			out.flush();
			sizeRead = in.read(buffer);
		}
		in.close();
		out.close();
		
		return sizeRead;
	}
}
