package org.onestonesoup.imagetools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.onestonesoup.core.ImageHelper;
import org.onestonesoup.core.process.CommandLineTool;

public class BatchConvertImages extends CommandLineTool {

	public BatchConvertImages(String[] args) {
		super(args);
	}

	@Override
	public int getMinimumArguments() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaximumArguments() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void process() {
		File sourceFolder = new File(getParameter(0));
		File targetFolder = new File(getParameter(1));
		
		for(File file: sourceFolder.listFiles()) {
			BufferedImage image;
			String fileName = file.getName();
			fileName = fileName.substring(0,fileName.indexOf("."));
			File outputFile = new File(targetFolder.getAbsolutePath()+"/"+fileName+".png");
			try {
				image = ImageHelper.loadImage(file);
				ImageHelper.savePNGImage(outputFile, image);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new BatchConvertImages(args);
	}

}
