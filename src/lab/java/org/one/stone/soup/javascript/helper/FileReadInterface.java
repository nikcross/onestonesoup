package org.one.stone.soup.javascript.helper;

public interface FileReadInterface {

	public abstract void setRoot(String root) throws Exception;

	public abstract String getCurrentTime();

	public abstract String getLastModified(String fileName);

	public abstract String getLength(String fileName);

	public abstract String load(String fileName);

	public abstract String[] listFiles(String directory);

	public abstract String[] listDirectories(String directory);

}