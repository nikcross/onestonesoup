package org.one.stone.soup.sds.serverbuilder;

import java.io.File;
import java.io.IOException;

import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.core.data.EntityTree;
import org.one.stone.soup.core.data.XmlHelper;
import org.one.stone.soup.core.data.XmlHelper.XmlParseException;
import org.one.stone.soup.process.CommandLineTool;

public class SinglePageWebAppBuilder extends CommandLineTool {

	public static void main(String[] args) {
		new SinglePageWebAppBuilder(args);
	}
	
	public SinglePageWebAppBuilder(String[] args) {
		super(args);
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public int getMaximumArguments() {
		return 0;
	}

	@Override
	public String getUsage() {
		return "build-xml-file";
	}

	@Override
	public void process() {
		try {
			File buildFile = new File(getParameter(0));
			String buildRoot = buildFile.getParentFile().getAbsolutePath();
			EntityTree build = XmlHelper.loadXml( buildFile );
			
			String pageTemplate = FileHelper.loadFileAsString(
					buildRoot+"/"+build.getChild("page-template").getValue().trim()
					);
			
			String[] scripts = build.getChild("scripts").getValue().split("\n");
			StringBuilder scriptData = new StringBuilder();
			for(String script: scripts) {
				script = script.trim();
				scriptData.append( FileHelper.loadFileAsString(buildRoot+"/scripts/"+script) );
			}
			pageTemplate = pageTemplate.replace("&script;", scriptData);
			
			FileHelper.saveStringToFile(pageTemplate,buildRoot+"/"+build.getChild("name").getValue()+".html");
			
		} catch (XmlParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
