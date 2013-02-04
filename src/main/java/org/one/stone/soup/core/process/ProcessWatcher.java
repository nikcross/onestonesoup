package org.one.stone.soup.core.process;

public interface ProcessWatcher {

	public void processMatch(String data);
	public void processEnd();
}
