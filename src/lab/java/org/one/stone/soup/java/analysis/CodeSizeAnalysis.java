package org.one.stone.soup.java.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.one.stone.soup.core.DirectoryHelper;
import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.process.CommandLineTool;

public class CodeSizeAnalysis extends CommandLineTool {

	public static final void main(String[] args) {
		new CodeSizeAnalysis(args);
	}	
	
	public CodeSizeAnalysis(String[] args) {
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
		return "[-R = recursive] <a root directory to search>";
	}

	@Override
	public void process() {
		File root = new File(getParameter(0));
		List<File> files = DirectoryHelper.findFiles(root.getAbsolutePath(), ".*\\.java", hasOption("R"));
				
		class JavaFile implements Comparable<JavaFile>{
			JavaFile(File file,int lines) {
				this.file = file;
				this.lines = lines;
			}
			File file;
			int lines;
			@Override
			public int compareTo(JavaFile j) {
				if(j.lines>lines) {
					return 1;
				} else {
					return -1;
				}
			}
		}
		
		List<JavaFile> jFiles = new ArrayList<JavaFile>();
		for(File file:files) {
			try {
				int lines = FileHelper.countLines(file);
				//System.out.println("File:"+file+" Lines:"+lines);
				jFiles.add( new JavaFile(file,lines) );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		JavaFile[] jFileArray = jFiles.toArray(new JavaFile[]{});
		Arrays.sort(jFileArray);
		
		for(JavaFile jFile: jFileArray) {
			System.out.println("File:"+jFile.file+" Lines:"+jFile.lines);
		}
	}

}
