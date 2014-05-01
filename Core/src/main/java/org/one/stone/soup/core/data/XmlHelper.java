package org.one.stone.soup.core.data;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.one.stone.soup.core.StringHelper;
import org.one.stone.soup.core.data.EntityTree.TreeEntity;


public class XmlHelper {

	public static class XmlParseException extends Exception {

		public XmlParseException(Exception e) {
			super(e);
		}
		private static final long serialVersionUID = 1L;
		
	}
	
	public static EntityTree loadXml(File file) throws XmlParseException, IOException{
		return loadXml( new FileInputStream(file),true );
	}
	
	public static EntityTree loadXml(InputStream in,boolean closeAtEnd) throws XmlParseException, IOException{
		try {

			EntityTree entityTree = new EntityTree( "root" );
			BufferedInputStream bin = new BufferedInputStream(in);
			TreeEntity entity = parseEntity( entityTree.getRoot(),bin );
			entityTree = new EntityTree(entity);
			
			return entityTree;
		} finally {
			if(closeAtEnd==true) {
				in.close();
			}
		}
	}

	private static TreeEntity parseEntity(TreeEntity parent,BufferedInputStream in) throws IOException {
		String prefix = cutTo(in,'<').trim();
		parent.setValue( prefix );
		String tag = cutTo(in,'>');
		String name = tag;
		if (tag.indexOf(" ")!=-1) {
			name = tag.substring(0, tag.indexOf(' '));
			tag = tag.substring(tag.indexOf(' ')+1);
		} else if(name.endsWith("/")||name.endsWith("?")) {
			name = name.substring(0,name.length()-1);
			tag = "/";
		} else {
			tag="";
		}
		if(name.startsWith("![CDATA[")) {
			parent.setValue(StringHelper.between(name, "![CDATA[", "]]"));
			return parent;
		}
		if(name.equals("/"+parent.getName())) {
			return null;
		}
		
		TreeEntity entity = parent.addChild(name);

		//Add Attributes
		tag = tag.trim();
		if(tag.length()>0) {
			tag = tag.replace(" =", "=").replace("= ", "=");
			String[] attributes = StringHelper.split(tag, " ", "\"", "\"");
			for(String attribute: attributes) {
				if(attribute.indexOf("=")==-1) {
					continue;
				}
				String key = attribute.substring(0,attribute.indexOf("="));
				attribute = attribute.substring(attribute.indexOf("=")+1);
				String value = attribute.substring(attribute.indexOf("\"")+1,attribute.lastIndexOf("\""));
				entity.setAttribute(key, value);
			}
		}
		
		if(tag.length()!=0 && (tag.lastIndexOf('/')==tag.length()-1||tag.lastIndexOf('?')==tag.length()-1)) {
			return entity;
		}
		
		TreeEntity child = parseEntity(entity,in);
		while(child!=null) {
			child = parseEntity(entity,in);
		}
		
		//String closeTag = parseTo(in,'>');
		//check close
		return entity;
	}
	
	private static String cutTo(InputStream in,char endCharacter) throws IOException {
		int data = in.read();
		StringBuffer prefix = new StringBuffer();
		while(data!=-1 && (char)data!=endCharacter) {
			prefix.append((char)data);
			data = in.read();
		}
		if(data==-1) {
			throw new IOException("Character '"+endCharacter+"' not found before end of stream.");
		}
		return prefix.toString();
	}
	
	public static EntityTree parseElement(String xml) throws XmlParseException {
		try{
			return loadXml(new ByteArrayInputStream(xml.getBytes()),false);
		} catch(Exception e) {
			throw new XmlParseException(e);
		}
	}

	public static String toXml(EntityTree element) {
		return toXml(element,true);
	}

	public static String toXml(EntityTree doc, boolean beautify) {
		StringBuffer result = new StringBuffer();
		
		bufferToXml(result,0,doc.getRoot(),beautify);
		
		return result.toString();
	}
	
	private static void bufferToXml(StringBuffer result,int depth,TreeEntity entity, boolean beautify) {
		String newLine = "";
		String tabs = "";
		if(beautify==true) {
			newLine="\n";
			tabs=StringHelper.repeat("\t",depth);
		}

		result.append(tabs);
		result.append("<"+entity.getName());
		Map<String,String> attributes = entity.getAttributes();
		for(String key: attributes.keySet()) {
			result.append(" "+key+"=\""+entity.getAttribute(key)+"\"");
			/*if(attributes.size()>1) {
				result.append(newLine);
			}*/
		}
		if(entity.hasValue()==false && entity.hasChildren()==false) {
			result.append("/>");
			return;
		} else {
			result.append(">");
		}
		
		if(entity.hasValue()) {
			result.append(entity.getValue());
		}
		if(entity.hasChildren()) {
			result.append(newLine);
			for(TreeEntity child: entity.getChildren()) {
				bufferToXml(result, depth+1, child, beautify);
				result.append(newLine);
			}
			result.append(tabs);
		}
		result.append("</"+entity.getName()+">");
	}

	public static Map<String,String> getChildAsMap(EntityTree tree, String name) {
		List<TreeEntity> list = tree.getChildren();
		Map<String,String> map = new HashMap<String,String>();
		for(TreeEntity entity: list) {
			map.put(entity.getAttribute(name),entity.getValue());
		}
		return map;
	}
}
