package org.one.stone.soup.javascript.helper;

public interface FileArchiveInterface extends FileWriteInterface {
	public void setFileService(FileWriteInterface fileService);
	public void setArchiveRoot(String root);
	public long[] getModifiedTimesForFile(String fileName);
	public String loadArchivedFile(String fileName,long modifiedTime);
	public String getArchiveNote(String fileName,long modifiedTime);
	public void setArchiveNote(String fileName,long modifiedTime,String note);
}
