package org.one.stone.soup.core.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectTree {

	public class ObjectNode {
		private String name;
		private String value; 
		private Map<String,String> attributes;
		private List<ObjectNode> children;
		
		private ObjectNode(String name) {
			this.name = name;
			attributes = new HashMap<String,String>();
			children = new ArrayList<ObjectNode>();
			value=null;
		}
		public String getName() {
			return name;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public boolean hasValue() {
			if(value!=null) {
				return true;
			} else {
				return false;
			}
		}
		public void setAttribute(String name,String value) {
			attributes.put(name, value);
		}
		public String getAttribute(String name) {
			return attributes.get(name);
		}
		public Map<String,String> getAttributes() {
			return attributes;
		}
		public boolean removeAttribute(String name) {
			if(getAttribute(name)==null) {
				return false;
			} else {
				attributes.remove(name);
				return true;
			}
		}
		public void removeAttributes() {
			attributes = new HashMap<String,String>();
		}
		public ObjectNode addChild(String name) {
			ObjectNode node = new ObjectNode(name);
			children.add(node);
			return node;
		}

		public boolean hasAttribute(String name) {
			if(getAttribute(name)!=null) {
				return true;
			} else {
				return false;
			}
		}
		public ObjectNode getChild(String name) {
			List<ObjectNode> matches = getChildren(name);
			if(matches.size()==1){
				return matches.get(0);
			} else {
				return null;
			}
		}
		public List<ObjectNode> getChildren(String name) {
			List<ObjectNode> matches = new ArrayList<ObjectNode>();
			
			for(ObjectNode node: children) {
				if(node.getName().equals(name)) {
					matches.add(node);
				}
			}
			return matches;
		}
		public List<ObjectNode> getChildren() {
			return children;
		}
		public boolean removeChild(String name) {
			ObjectNode target = getChild(name);
			if(target==null) {
				return false;
			} else {
				children.remove(target);
				return true;
			}
		}
		public void removeChildren(String name) {
			List<ObjectNode> matches = getChildren(name);
			for(ObjectNode target: matches) {
				children.remove(target);
			}
		}
		public void removeChildren() {
			children = new ArrayList<ObjectNode>();
		}
	}
	
	private ObjectNode root;
	
	public ObjectTree(String name) {
		root = new ObjectNode(name);
	}
	
	public ObjectNode addChild(String name) {
		return root.addChild(name);
	}

	public String getName() {
		return root.getName();
	}

	public String getValue() {
		return root.getValue();
	}

	public void setValue(String value) {
		root.setValue(value);
	}

	public boolean hasValue() {
		return root.hasValue();
	}

	public void setAttribute(String name, String value) {
		root.setAttribute(name, value);
	}

	public String getAttribute(String name) {
		return root.getAttribute(name);
	}

	public Map<String, String> getAttributes() {
		return root.getAttributes();
	}

	public boolean removeAttribute(String name) {
		return root.removeAttribute(name);
	}

	public void removeAttributes() {
		root.removeAttributes();
	}

	public boolean hasAttribute(String name) {
		return root.hasAttribute(name);
	}

	public ObjectNode getChild(String name) {
		return root.getChild(name);
	}

	public List<ObjectNode> getChildren(String name) {
		return root.getChildren(name);
	}

	public List<ObjectNode> getChildren() {
		return root.getChildren();
	}

	public boolean removeChild(String name) {
		return root.removeChild(name);
	}

	public void removeChildren(String name) {
		root.removeChildren(name);
	}

	public void removeChildren() {
		root.removeChildren();
	}
	
}
