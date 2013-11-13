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
}
