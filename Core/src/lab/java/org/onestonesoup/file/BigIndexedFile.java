package org.onestonesoup.file;

import static org.onestonesoup.core.constants.SizeConstants.MEGABYTE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BigIndexedFile {

	private String name;
	
	private long indexPartitionSize = 100*MEGABYTE;
	private long dataPartitionSize = 100*MEGABYTE;
	private long indexRecordSize = 8;
	
	private class IndexInfo {
		private long index;
		private File indexFile;
		private int indexFileOffset = -1;
		private File dataFile;
		private int dataFileStartPointer = -1;
		private int dataSize = -1;
		private IndexInfo(long index) {
			
		}
	}
	
	public BigIndexedFile(String name) {
		this.name = name;
	}
	
	public void store(byte[] data, long index) throws IOException {
		IndexInfo indexInfo = createIndexInfo(index);
		// lock
		
		// store the data
		storeData(data,indexInfo);
		
		// store an index to the data
		writeIndexRecord(indexInfo);
		
		//unlock
	}
	
	public byte[] retrieve(long index) throws IOException {
		IndexInfo indexInfo = createIndexInfo(index);
		//lock
		
		// retrieve the data
		byte[] data = retrieveData(indexInfo);
		
		//unlock
		
		return data;
	}
	

	private IndexInfo createIndexInfo(long index) {
		//convert an index into index info
		IndexInfo indexInfo = new IndexInfo(index);
		
		//set index file
		
		return indexInfo;
	}
	
	private byte[] retrieveData(IndexInfo indexInfo) throws IOException {
		//lookup data file pointers
		readIndexRecord(indexInfo);
		
		//load the data
		byte[] data = readData(indexInfo);
		
		//return the data
		return data;
	}
	
	private void storeData(byte[] data,IndexInfo indexInfo) throws IOException {
		//write data
		writeData(indexInfo, data);
		
		//write index record
		writeIndexRecord(indexInfo);
	}
	
	private byte[] readData(IndexInfo indexInfo) throws IOException {
		RandomAccessFile randomAccessFile = new RandomAccessFile(indexInfo.indexFile,"r");
		randomAccessFile.seek(indexInfo.dataFileStartPointer);
		byte[] data = new byte[indexInfo.dataSize];
		randomAccessFile.read(data,0,indexInfo.dataSize);
		randomAccessFile.close();
		
		return data;
	}
	
	private void writeData(IndexInfo indexInfo,byte[] data) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(indexInfo.dataFile,true);
		fileOutputStream.write(data);
		fileOutputStream.flush();
		fileOutputStream.close();
		indexInfo.dataSize = indexInfo.dataFileStartPointer+data.length;
	}
	
	private void readIndexRecord(IndexInfo indexInfo) throws IOException {
		RandomAccessFile randomAccessFile = new RandomAccessFile(indexInfo.indexFile,"r");
		randomAccessFile.seek(indexInfo.indexFileOffset);
		long dataFileStartPointer = randomAccessFile.readLong();
		long dataSize = randomAccessFile.readLong();
		randomAccessFile.close();
		
		indexInfo.dataFile = new File(name+(dataFileStartPointer/dataPartitionSize));
		indexInfo.dataFileStartPointer = (int)(dataFileStartPointer%dataPartitionSize);
		indexInfo.dataSize = (int)dataSize;
	}
	
	private void writeIndexRecord(IndexInfo indexInfo) throws FileNotFoundException, IOException {
		//if smaller than required
		padFileToSize(indexInfo.indexFile,indexInfo.indexFileOffset);
		//write using random access
		RandomAccessFile randomAccessFile = new RandomAccessFile(indexInfo.indexFile,"rw");
		randomAccessFile.seek(indexInfo.indexFileOffset);
		randomAccessFile.writeLong(indexInfo.dataFileStartPointer);
		randomAccessFile.writeLong(indexInfo.dataSize);
		randomAccessFile.close();
	}
	
	private void padFileToSize(File file, long size) throws FileNotFoundException, IOException {
		int padSize = (int)(size-file.length());
		if(padSize>0) {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(new byte[padSize]);
			fileOutputStream.flush();
			fileOutputStream.close();
		}
	}
}
