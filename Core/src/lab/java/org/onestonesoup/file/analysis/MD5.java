/**
 * 
 */
package org.onestonesoup.file.analysis;

import java.io.File;

import org.onestonesoup.core.FileHelper;
import org.onestonesoup.core.process.CommandLineTool;

public class MD5 extends CommandLineTool {

	public static void main(String[] args) {
		new MD5(args);
	}

	public MD5(String[] args) {
		super(args);
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public int getMaximumArguments() {
		return 1;
	}

	@Override
	public String getUsage() {
		return "<a file to calculate MD5 checksum for>";
	}

	@Override
	public void process() {
		File file = new File(getParameter(0));
		
		String check = FileHelper.generateMD5Checksum(file);
		System.out.println("MD5 for "+file.getAbsolutePath());
		System.out.println("["+check+"]");
	}
	
}
