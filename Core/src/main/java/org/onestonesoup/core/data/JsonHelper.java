package org.onestonesoup.core.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.onestonesoup.core.StringHelper;

/**
 * Created by nikcross on 26/06/17.
 */
public class JsonHelper {

	public static void objectToStream(EntityTree.TreeEntity entity,OutputStream out) throws IOException {
		objectToStream(entity,out,0);
	}

	private static void objectToStream(EntityTree.TreeEntity entity,OutputStream out,int depth) throws IOException {
		out.write("\n".getBytes());
		StringHelper.repeat(" ",depth);
		out.write("{".getBytes());
		boolean first = true;
		for(EntityTree.TreeEntity child: entity.getChildren()) {
			if(!first) out.write(",".getBytes());
			first=false;
			out.write("\"".getBytes());
			out.write(child.getName().getBytes());
			out.write("\": ".getBytes());
			if(child.hasChildren()) {
				if(child.getAttribute("array")!=null) {
					arrayToStream(child,out);
				} else {
					objectToStream(child,out);
				}
			} else {
				if(child.getValue()!=null) {
					out.write("\"".getBytes());
					out.write(child.getValue().getBytes());
					out.write("\"".getBytes());
				} else {
					out.write("null".getBytes());
				}
			}
		}
		out.write("}\n".getBytes());
	}
	public static String stringifyObject(EntityTree.TreeEntity entity) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			objectToStream(entity,stream);
		} catch (IOException e) {}
		return new String(stream.toByteArray());
	}

	public static void arrayToStream(EntityTree.TreeEntity entity, OutputStream out) throws IOException {
		arrayToStream(entity,out,0);
	}

	private static void arrayToStream(EntityTree.TreeEntity entity, OutputStream out, int depth) throws IOException {
		out.write("\n".getBytes());
		StringHelper.repeat(" ",depth);
		out.write("[".getBytes());

		boolean first = true;
		for(EntityTree.TreeEntity child: entity.getChildren()) {
			if(!first) out.write(", ".getBytes());
			first = false;
			if(child.hasChildren()) {
				if(child.getAttribute("array")!=null) {
					arrayToStream(child,out,depth+1);
				} else {
					objectToStream(child,out,depth+1);
				}
			} else {
				if(child.getValue()!=null) {
					out.write("\"".getBytes());
					out.write(child.getValue().getBytes());
					out.write("\"".getBytes());
				} else {
					out.write("null".getBytes());
				}
			}
		}

		out.write("]".getBytes());
	}

	public static String stringifyArray(EntityTree.TreeEntity entity) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			arrayToStream(entity,stream);
		} catch (IOException e) {}
		return new String(stream.toByteArray());
	}

	public static EntityTree parseObject(String name, String data) {
		data = data.trim();
		EntityTree entityTree = new EntityTree(name);

		if(data.startsWith("{")) {
			parseNamedObject( entityTree.getRoot(), data.substring(data.indexOf("{")+1,data.lastIndexOf("}")) );
		}

		return entityTree;
	}

	private static void parseNamedObject(EntityTree.TreeEntity parent,String allData) {
		List<String> parts = split(allData);

		for(String data: parts) {

			String name = data.substring(0, data.indexOf(":")).trim();
			if (name.startsWith("\"")) {
				name = name.substring(1, name.lastIndexOf("\""));
			}

			data = data.substring(data.indexOf(":") + 1).trim();

			EntityTree.TreeEntity object = parent.addChild(name);

			if (data.startsWith("{")) {
				parseNamedObject(object, data.substring(data.indexOf("{")+1, data.lastIndexOf("}")));
			} else if (data.startsWith("[")) {
				parseArray(object, data.substring(data.indexOf("[")+1, data.lastIndexOf("]")));
			} else {
				if (data.startsWith("\"")) {
					object.setValue(data.substring(1, data.lastIndexOf("\"")));
				} else {
					object.setValue(data);
				}
			}
		}
	}

	private static void parseArray(EntityTree.TreeEntity object, String substring) {
		object.setAttribute("array","true");
		List<String> parts = split(substring);
		object.setAttribute("length", ""+parts.size());

		for(int i=0; i<parts.size(); i++) {
			String data = parts.get(i).trim();
			EntityTree.TreeEntity item = object.addChild(""+i);
			if (data.startsWith("{")) {
				parseNamedObject(item, data.substring(data.indexOf("{")+1, data.lastIndexOf("}")));
			} else if (data.startsWith("[")) {
				parseArray(item, data.substring(data.indexOf("[")+1, data.lastIndexOf("]")));
			} else {
				if (data.startsWith("\"")) {
					item.setValue(data.substring(1, data.lastIndexOf("\"")));
				} else {
					item.setValue(data);
				}
			}
		}
	}

	private static List<String> split(String data) {
		List<String> parts = new ArrayList();
		String part = "";
		int depth = 0;
		for(byte c: data.getBytes()) {
			if(depth!=0) {
				if(c=='}' || c==']') {
					depth--;
				} else if(c=='{' || c=='[') {
					depth++;
				}
				part += Character.toString((char) c);
			} else if(c==',') {
				parts.add(part);
				part = "";
			} else if(c=='{' || c=='[') {
				part += Character.toString((char)c);
				depth++;
			} else {
				part += Character.toString((char)c);
			}
		}
		parts.add(part);
		return parts;
	}

}
