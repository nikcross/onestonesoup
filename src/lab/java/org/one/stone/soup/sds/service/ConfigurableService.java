package org.one.stone.soup.sds.service;

public interface ConfigurableService {

	void loadConfiguration();
	void saveConfiguration();
	void setConfigurationParameter(String key,String value);
	String getConfigurationParameter(String key);
	String[] getConfigurationParameters();
}
