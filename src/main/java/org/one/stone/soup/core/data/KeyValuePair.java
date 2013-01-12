package org.one.stone.soup.core.data;

public class KeyValuePair {
	private String key;
	private String value;
	
	public KeyValuePair(String key,String value) {
		this.key = key;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}
	
}
