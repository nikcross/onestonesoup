package org.one.stone.soup.javascript.helper;

public interface FileWriteInterface {

	public abstract boolean save(String fileName, String data);

	public abstract boolean delete(String fileName);

	public abstract boolean createDirectory(String directory);

}