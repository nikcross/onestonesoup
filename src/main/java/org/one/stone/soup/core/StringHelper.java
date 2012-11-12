package org.one.stone.soup.core;

public class StringHelper {

	public static String padLeftToFitSize(String data,char pad,int size) {
		while(data.length()<size) {
			data = pad+data;
		}
		return data;
	}
	public static String padRightToFitSize(String data,char pad,int size) {
		while(data.length()<size) {
			data = data+pad;
		}
		return data;
	}
}
