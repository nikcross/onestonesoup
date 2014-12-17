package org.onestonesoup.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	public static String asHex(byte[] data) {

        StringBuffer b = new StringBuffer();

        for(int loop=0;loop<data.length;loop++)
        {
                String txt = Integer.toHexString(data[loop]);
                if(txt.length()>2)
                        txt = txt.substring(txt.length()-2);
                b.append( "0x"+txt+" " );
        }

        return b.toString();
	}
	public static String repeat(String data, int number) {
		return new String(new char[number]).replace("\0", data);
	}
	public static String arrayToString(String[] array,String delimeter) {
		StringBuilder builder = new StringBuilder();
		for(String part: array) {
			if(builder.length()>0) {
				builder.append(delimeter);
			}
			builder.append(part);
		}
		return builder.toString();
	}
	public static String before(String data, String value) {
		if(data.indexOf(value)==-1) {
			return null;
		}
		return data.substring(0,data.indexOf(value));
	}
	public static String after(String data, String value) {
		return data.substring(data.indexOf(value)+value.length());
	}
	public static String between(String data, String start,String end) {
		return before(after(data,start),end);
	}
	public static String decodeBase64(String data) throws IOException {
		return new String(Base64.decode(data));
	}
	public static String encodeBase64(String data) throws IOException {
		return Base64.encode(data);
	}
	public static String arrayToString(String[] parts, String delimeter, int start,
			int end) {
		StringBuilder builder = new StringBuilder();
		for(int i=start;i<end;i++) {
			if(builder.length()>0) {
				builder.append(delimeter);
			}
			builder.append(parts[i]);
		}
		return builder.toString();
	}
	public static String truncate(String text, int maxLength, String endWith) {
		if(text.length()>maxLength) {
			text = text.substring(0,maxLength-endWith.length());
			text += endWith;
		}
		return text;
	}
	public static String[] split(String data, String delimeter, String ignoreStart, String ignoreEnd) {
		List<String> list = new ArrayList<String>();
		String currentElement = "";
		for(int i=0;i<data.length();) {
			if(data.substring(i).startsWith(ignoreStart)) {
				String part = before(data.substring(i+1),ignoreEnd);
				part = ignoreStart+part+ignoreEnd;
				i+=part.length();
				currentElement+=part;
			} else if(data.substring(i).startsWith(delimeter)) {
				list.add(currentElement);
				currentElement="";
				i+=delimeter.length();
			} else {
				currentElement+=data.substring(i,i+1);
				i++;
			}
		}
		list.add(currentElement);
		
		return list.toArray(new String[]{});
	}
}
