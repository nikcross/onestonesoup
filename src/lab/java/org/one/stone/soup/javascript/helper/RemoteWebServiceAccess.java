package org.one.stone.soup.javascript.helper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.core.StringHelper;

public class RemoteWebServiceAccess {

	@JSMethodHelp(signature="<URL of the resource>")
	public static String getURLAsString(String url) throws IOException {
		URL u = new URL(url);
		URLConnection c = u.openConnection();
		return FileHelper.loadFileAsString(c.getInputStream());
	}
	
	@JSMethodHelp(signature="<URL of the resource>,<user name>,<password>")
	public static String getURLAsString(String url,String user,String password) throws IOException {
		@SuppressWarnings("restriction")
		String code = new sun.misc.BASE64Encoder().encode((user+":"+password).getBytes());
		
		URL u = new URL(url);
		URLConnection c = u.openConnection();
		c.setRequestProperty("Authorization", "Basic " + code);
		return FileHelper.loadFileAsString(c.getInputStream());
	}
	
	@JSMethodHelp(signature="<URL of the resource>,<file name to save the resource to>")
	public static void getURLAsFile(String url,String fileName) throws IOException {
		URL u = new URL(url);
		URLConnection c = u.openConnection();
		FileHelper.copyInputStreamToFile(c.getInputStream(),new File(fileName));
	}
	
	@JSMethodHelp(signature="<URL of the resource>,<user name>,<password>,<file name to save the resource to>")
	public static void getURLAsFile(String url,String user,String password,String fileName) throws IOException {
		String code = StringHelper.encodeBase64(user+":"+password);
		
		URL u = new URL(url);
		URLConnection c = u.openConnection();
		c.setRequestProperty("Authorization", "Basic " + code);
		FileHelper.copyInputStreamToFile(c.getInputStream(),new File(fileName));
	}
}
