package org.one.stone.soup.file.crawler;

import org.one.stone.soup.process.CommandLineTool;

public class DirSync extends CommandLineTool {

	public static void main(String[] args) {
		new DirSync(args);
	}
	
	public DirSync(String[] args) {
		super(args);
	}

	@Override
	public int getMinimumArguments() {
		return 3;
	}

	@Override
	public int getMaximumArguments() {
		return 3;
	}

	@Override
	public String getUsage() {
		return "[-D (will delete files)] logPath directoryA directoryB";
	}

	@Override
	public void process() {
		DirectorySynchronizer directorySynchronizer = new DirectorySynchronizer();
		directorySynchronizer.setLogPath(getParameter(0) );
		directorySynchronizer.setDirectoryA( getParameter(1) );
		directorySynchronizer.setDirectoryB( getParameter(2) );
		if(hasOption("D")) {
			directorySynchronizer.setDeleteFiles(true);
		}
		directorySynchronizer.start();
	}

}
