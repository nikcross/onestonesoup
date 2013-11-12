package org.one.stone.soup.core.data;

public class KeyValuePair {
	private String key;
	private String value;

	public static KeyValuePair parseKeyAndValue(String data) {
		return parseKeyAndValue(data, "=");
	}
	
	public static KeyValuePair parseKeyAndValue(String data,String separator) {
		String[] parts = data.split(separator);
		String key = parts[0];
		String value = parts[1];
		
		return new KeyValuePair(key, value);
	}
	
	public KeyValuePair(String key,String value) {
		this.setKey(key);
		this.setValue(value);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}