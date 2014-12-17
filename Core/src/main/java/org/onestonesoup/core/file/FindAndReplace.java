package org.onestonesoup.core.file;

import java.io.File;
import java.io.IOException;

import org.onestonesoup.core.FileHelper;
import org.onestonesoup.core.file.DirectoryCrawler.FileListener;
import org.onestonesoup.core.process.CommandLineTool;

public class FindAndReplace extends CommandLineTool implements FileListener {

	public static void main(String[] args) {
		new FindAndReplace(args);
	}
	
	private String find;
	private String replace;
	
	public FindAndReplace(String[] args) {
		super(args);
	}

	@Override
	public int getMinimumArguments() {
		return 2;
	}

	@Override
	public int getMaximumArguments() {
		return 2;
	}

	@Override
	public String getUsage() {
		return "FindAndReplace <find> <replace> [-d=<directory>]";
	}

	@Override
	public void process() {
		DirectoryCrawler crawler = new DirectoryCrawler();
		
		find = getParameter(0);
		replace = getParameter(1);
		
		crawler.addFileListener(this);
		File path = new File(new File(getOption("d")).getAbsolutePath());
		System.out.println("Crawling "+path);
		
		crawler.crawl(path);
	}

	@Override
	public void fileFound(File file) {
		if ( ! file.getName().endsWith(".java")) {
			return;
		}
		try {
			String data = FileHelper.loadFileAsString(file);
			if(data.indexOf(find)==-1) {
				return;
			}
			
			data = data.replaceAll(find, replace);
			System.out.println("changeing "+file);
			FileHelper.saveStringToFile(data, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
