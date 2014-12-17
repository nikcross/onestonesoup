package org.onestonesoup.core.javascript;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.onestonesoup.core.StringHelper;
import org.onestonesoup.core.data.EntityTree;
import org.onestonesoup.core.data.JavaTree;
import org.onestonesoup.core.data.KeyValuePair;
import org.onestonesoup.core.data.EntityTree.TreeEntity;

/**
 * @author nikcross
 *
 */
public class JSONHelper {
	
	public static String toJSON(EntityTree tree) {
		return toJSON(tree.getRoot());
	}
	
	public static String toJSON(EntityTree.TreeEntity node) {
		StringBuilder data = new StringBuilder(node.getName());
		data.append(" {");
		
		Map<String,String> attributes = node.getAttributes();
		boolean first = true;
		for(String key: attributes.keySet()) {
			if(!first) {
				data.append(",");
			}
			String value = attributes.get(key);
			data.append(" "+key+": \""+value+"\"");
			
			first = false;
		}
		for(EntityTree.TreeEntity child: node.getChildren()) {
			if(!first) {
				data.append(",");
			}
			
			data.append(" "+toJSON(child));
			
			first = false;
		}
		
		data.append("}");
		
		return data.toString();
	}
	
	public static EntityTree toTree(String json) throws IOException {
		EntityTree temp = new EntityTree("temp");
		parseEntity(temp.getRoot(), json);
		
		EntityTree result = new EntityTree( temp.getRoot().getChildren().get(0) );
		
		return result; 
	}
	
	public static String toJSON(Object instance) {
		return toJSON( JavaTree.toEntityTree(instance) );
	}
	
	public static Object fromJSON(String json) throws IOException {
		EntityTree tree = toTree(json);
		return JavaTree.toObject(tree);
	}
	
	private static TreeEntity parseEntity(TreeEntity parent,String data) throws IOException {
		String name = StringHelper.before(data, ":").trim();
		TreeEntity child = parent.addChild(name);
		
		data = StringHelper.after(data, "{");
	
		String[] parts = data.split(",");
		for(String part: parts) {
			if(part.contains("{")) {
				parseEntity(child, part);
			} else {
				KeyValuePair kvp = KeyValuePair.parseKeyAndValue(part, ":");
				child.setAttribute(kvp.getKey().trim(), kvp.getValue().trim());
			}
		}
		
		return child;
	}
}
