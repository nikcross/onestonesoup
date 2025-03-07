package org.onestonesoup.core.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ProcessWatch implements Runnable {

	public Map<String,ProcessWatcher> matchers = new HashMap<String,ProcessWatcher>();
	private Process process;
	private InputStream stream;
	
	public ProcessWatch() {}
	
	public void executeInDirectory(String directory,String command) throws Exception {
		process = Runtime.getRuntime().exec(command, new String[]{}, new File(directory));
		watch(process.getInputStream());
	}
	
	public void execute(String command) throws Exception {
		process = Runtime.getRuntime().exec(command);
		watch(process.getInputStream());
	}

	public boolean kill() throws InterruptedException {
		process.destroy();
		int count = 0;
		//Be nice and give it 30 seconds to end
		while( process.isAlive() && count<30 ) {
			Thread.sleep( 1000 );
		}
		//Else force it to stop
		if( process.isAlive() ) {
			process.destroyForcibly();
		}
		return process.isAlive();
	}
	private void watch(InputStream stream) {
		this.stream = stream;
		
		new Thread(this).start();
	}
	
	public Map<String,ProcessWatcher> getMatchers() {
		return matchers;
	}
	
	public void addMatcher(String matcher,ProcessWatcher watcher) {
		matchers.put(matcher,watcher);
	}
	
	public void run() {

		try{
			BufferedReader reader = new BufferedReader( new InputStreamReader(stream));
			
			String line = reader.readLine();
			while(line!=null) {
				
				for(String matcher: getMatchers().keySet()) {
					if(line.matches(matcher)) {
						getMatchers().get(matcher).processMatch(line);
					}
				}
				
				line = reader.readLine();
			}
		} catch (Exception e) {
			
		}
		
		while(process.isAlive()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for(String matcher: getMatchers().keySet()) {
			getMatchers().get(matcher).processEnd(process.exitValue());
		}
	}
}
