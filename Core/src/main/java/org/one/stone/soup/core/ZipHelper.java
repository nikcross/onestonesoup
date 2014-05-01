package org.one.stone.soup.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipHelper {

	public class Zip {
		private	ZipOutputStream zOut;
		
		private Zip(String fileName) throws FileNotFoundException {
			OutputStream out = new FileOutputStream(fileName);
			zOut = new ZipOutputStream(out);
			zOut.setLevel(9);
			zOut.setMethod(Deflater.DEFLATED);
		}

		public Zip createFolder(String folderName) throws IOException {
			folderName = folderName.replace('\\','/');

				if(folderName.length()<1 || folderName.charAt(folderName.length()-2)!='/')
				{
					folderName = folderName+"/";
				}
				ZipEntry zEntry = new ZipEntry(folderName);
				zOut.putNextEntry(zEntry);
				return this;
		}
		
		public Zip addFileAs(File file,String fileName) throws IOException {
			fileName = fileName.replace('\\','/');

			if(file.isDirectory())
			{
				if(fileName.length()<1 || fileName.charAt(fileName.length()-2)!='/')
				{
					fileName = fileName+"/";
				}
				ZipEntry zEntry = new ZipEntry(fileName);
				try{
					zOut.putNextEntry(zEntry);
					
					for(File nextFile: file.listFiles()) {
						addFileAs( nextFile,nextFile.getName() );
					}
				}
				catch(Exception e)
				{
				}
				return this;
			}

			FileInputStream in = new FileInputStream(file);

			ZipEntry zEntry = new ZipEntry(fileName);

			zEntry.setMethod(ZipEntry.DEFLATED);

			zOut.putNextEntry(zEntry);

			byte[] data = new byte[10000];

			int inByte = in.read(data);

			while(inByte!=-1)
			{
				zOut.write(data,0,inByte);

				inByte = in.read(data);
			}

			zOut.closeEntry();

			in.close();
			
			return this;
		}
		public Zip addInputStreamAs(InputStream in,String fileName) throws IOException {
			fileName = fileName.replace('\\','/');

			ZipEntry zEntry = new ZipEntry(fileName);

			zEntry.setMethod(ZipEntry.DEFLATED);

			zOut.putNextEntry(zEntry);

			byte[] data = new byte[10000];

			int inByte = in.read(data);

			while(inByte!=-1)
			{
				zOut.write(data,0,inByte);

				inByte = in.read(data);
			}

			zOut.closeEntry();

			in.close();
			
			return this;
		}
		public void close() throws IOException {
			zOut.close();
		}
	}
	
	private static ZipHelper zipHelper = new ZipHelper();
	private Zip getZipFile(String fileName) throws FileNotFoundException {
		return new Zip(fileName);
	}
	
	public static Zip createZipFile(File file) throws FileNotFoundException {
		Zip zipFile = zipHelper.getZipFile(file.getAbsolutePath());
		return zipFile;
	}
	
	public static void unzipFileToFolderAndBackupToZipFile(File file,File folder,File backupFile) throws IOException {
		Zip backup = createZipFile(backupFile);
		ZipFile zipFile = new ZipFile( file );
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
			
			if(	entry.isDirectory() )
			{
				new File(
						folder.getAbsolutePath()+"/"+entry.getName()
					).mkdirs();
			}
			else
			{
				File testFile = new File(folder.getAbsolutePath()+"/"+entry.getName());
				if( testFile.exists() )
				{
					backup.addFileAs(testFile, entry.getName());
				}
				
				copy(
						zipFile.getInputStream(entry),
						folder.getAbsolutePath()+"/"+entry.getName()
					);
			}
		}
		
		backup.close();
		zipFile.close();
	}
	
	public static void unzipFileToFolder(File file,File folder) throws IOException {
		ZipFile zipFile = new ZipFile( file );
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry)entries.nextElement();
			
			if(	entry.isDirectory() )
			{
				new File(
						folder.getAbsolutePath()+"/"+entry.getName()
					).mkdirs();
			}
			else
			{
				copy(
						zipFile.getInputStream(entry),
						folder.getAbsolutePath()+"/"+entry.getName()
				);
			}
		}
		zipFile.close();
	}
	
	private static void copy(InputStream in,String fileName) throws IOException {
		FileOutputStream oStream = new FileOutputStream(new File(fileName));

		byte[] buffer = new byte[1000];
		int sizeRead = in.read(buffer);
		while(sizeRead>0)
		{
			oStream.write(buffer,0,sizeRead);
			oStream.flush();
			sizeRead = in.read(buffer);
		}
		oStream.close();
	}
}
