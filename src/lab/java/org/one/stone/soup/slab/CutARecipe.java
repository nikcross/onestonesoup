package org.one.stone.soup.slab;

import java.io.IOException;

import org.one.stone.soup.core.FileHelper;
import org.one.stone.soup.core.StringHelper;
import org.one.stone.soup.process.CommandLineTool;

public class CutARecipe extends CommandLineTool{

	public static void main(String[] args) {
		new CutARecipe(args);
	}
	
	public CutARecipe(String[] args) {
		super(args);
		// TODO Auto-generated constructor stub
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
		String data;
		try {
			
			data = FileHelper.loadFileAsString("src/lab/resources/cutter/recipe.html");
			String ingredients = StringHelper.between(data, "div id=\"ingredients\"", "</div");
			String instructions = StringHelper.between(data, "<ol class=\"instructions\">", "</ol>");
			
			while(ingredients.length()>0) {
				String ingredient = StringHelper.between(ingredients, "<p class=\"ingredient\">", "</p>");
				if(ingredient==null) {
					break;
				}
				//ingredient = ingredient.replaceFirst("<.*>",""); // TODO
				System.out.println("f:"+ingredient);
				
				ingredients = StringHelper.after(ingredients,"<p class=\"ingredient\">");
				ingredients = StringHelper.after(ingredients,"</p>");
			}
			
			while(instructions.length()>0) {
				String instruction = StringHelper.between(instructions, "<li class=\"instruction\">", "</li>");
				instruction = StringHelper.between(instruction,"<p>","</p>");
				if(instruction==null) {
					break;
				}
				System.out.println("i:"+instruction);
				
				instructions = StringHelper.after(instructions,"<li class=\"instruction\">");
				instructions = StringHelper.after(instructions,"</li>");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
