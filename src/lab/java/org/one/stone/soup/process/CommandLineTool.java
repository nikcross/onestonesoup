package org.one.stone.soup.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CommandLineTool {
	
	public CommandLineTool(String[] args) {
		initialise(args);
		process();
	}
	
	private void initialise(String[] args) {
		for(String argument: args) {
			if(argument.startsWith("-")) {
				String[] parts = argument.substring(1).split("=");
				if(parts.length==1) {
					options.put(parts[0],"");
				} else {
					options.put(parts[0],parts[1]);
				}
			} else {
				parameters.add(argument);
			}
		}
		
		if( parameters.size()<getMinimumArguments() && parameters.size()<getMaximumArguments() ) {
			displayUsage();
			return;
		}
		
		process();
	}

	private Map<String,String> options = new HashMap<String,String>();
	private List<String> parameters = new ArrayList<String>();
	
	public void displayUsage() {
		System.out.println("Usage: "+getUsage());
	}
	
	public String getOption(int index) {
		return options.get(index);
	}
	
	public boolean hasOption(String name) {
		if(options.containsKey(name)) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getParameter(int index) {
		return parameters.get(index);
	}
	
	public abstract int getMinimumArguments();
	public abstract int getMaximumArguments();
	public abstract String getUsage();
	public abstract void process();
}
