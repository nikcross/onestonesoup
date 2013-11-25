StringHelper = org.one.stone.soup.core.StringHelper;
FileHelper = org.one.stone.soup.core.FileHelper;

data = FileHelper.loadFileAsString("cutter/recipe.html");
var ingredients = StringHelper.between(data, "div id=\"ingredients\"", "</div");
var instructions = StringHelper.between(data, "<ol class=\"instructions\">", "</ol>");

while(ingredients.length()>0) {
	var ingredient = StringHelper.between(ingredients, "<p class=\"ingredient\">", "</p>");
	if(ingredient==null) {
		break;
	}
	//ingredient = ingredient.replaceFirst("<.*>",""); // TODO
	out.println("f:"+ingredient);
	
	ingredients = StringHelper.after(ingredients,"<p class=\"ingredient\">");
	ingredients = StringHelper.after(ingredients,"</p>");
}

while(instructions.length()>0) {
	instruction = StringHelper.between(instructions, "<li class=\"instruction\">", "</li>");
	if(instruction==null) {
		break;
	}
	instruction = StringHelper.between(instruction,"<p>","</p>");
	if(instruction==null) {
		break;
	}
	out.println("i:"+instruction);
	
	instructions = StringHelper.after(instructions,"<li class=\"instruction\">");
	instructions = StringHelper.after(instructions,"</li>");
}

