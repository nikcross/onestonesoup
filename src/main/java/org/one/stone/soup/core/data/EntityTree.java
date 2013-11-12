package org.one.stone.soup.core.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityTree {

	public class TreeEntity {
		private String name;
		private String value; 
		private Map<String,String> attributes;
		private List<TreeEntity> children;
		
		private TreeEntity(String name) {
			this.name = name;
			attributes = new HashMap<String,String>();
			children = new ArrayList<TreeEntity>();
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
		public TreeEntity addChild(String name) {
			TreeEntity node = new TreeEntity(name);
			children.add(node);
			return node;
		}

		public TreeEntity addChild(TreeEntity entity) {
			children.add(entity);
			return entity;
		}

		public boolean hasAttribute(String name) {
			if(getAttribute(name)!=null) {
				return true;
			} else {
				return false;
			}
		}
		public TreeEntity getChild(String name) {
			List<TreeEntity> matches = getChildren(name);
			if(matches.size()==1){
				return matches.get(0);
			} else {
				return null;
			}
		}
		public List<TreeEntity> getChildren(String name) {
			List<TreeEntity> matches = new ArrayList<TreeEntity>();
			
			for(TreeEntity node: children) {
				if(node.getName().equals(name)) {
					matches.add(node);
				}
			}
			return matches;
		}
		public List<TreeEntity> getChildren() {
			return children;
		}
		public boolean removeChild(String name) {
			TreeEntity target = getChild(name);
			if(target==null) {
				return false;
			} else {
				children.remove(target);
				return true;
			}
		}
		public void removeChildren(String name) {
			List<TreeEntity> matches = getChildren(name);
			for(TreeEntity target: matches) {
				children.remove(target);
			}
		}
		public void removeChildren() {
			children = new ArrayList<TreeEntity>();
		}
		public boolean hasChildren() {
			if(children.size()==0) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	private TreeEntity root;
	
	public EntityTree(String name) {
		root = new TreeEntity(name);
	}
	
	public EntityTree(TreeEntity root) {
		this.root = root;
	}
	
	public TreeEntity addChild(String name) {
		return root.addChild(name);
	}

	public TreeEntity addChild(TreeEntity entity) {
		return root.addChild(entity);
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

	public TreeEntity getChild(String name) {
		return root.getChild(name);
	}

	public List<TreeEntity> getChildren(String name) {
		return root.getChildren(name);
	}

	public List<TreeEntity> getChildren() {
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

	public TreeEntity getRoot() {
		return root;
	}
	
}
