package org.one.stone.soup.core.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityTree {

	public class Entity {
		private String name;
		private String value; 
		private Map<String,String> attributes;
		private List<Entity> children;
		
		private Entity(String name) {
			this.name = name;
			attributes = new HashMap<String,String>();
			children = new ArrayList<Entity>();
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
		public Entity addChild(String name) {
			Entity node = new Entity(name);
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
		public Entity getChild(String name) {
			List<Entity> matches = getChildren(name);
			if(matches.size()==1){
				return matches.get(0);
			} else {
				return null;
			}
		}
		public List<Entity> getChildren(String name) {
			List<Entity> matches = new ArrayList<Entity>();
			
			for(Entity node: children) {
				if(node.getName().equals(name)) {
					matches.add(node);
				}
			}
			return matches;
		}
		public List<Entity> getChildren() {
			return children;
		}
		public boolean removeChild(String name) {
			Entity target = getChild(name);
			if(target==null) {
				return false;
			} else {
				children.remove(target);
				return true;
			}
		}
		public void removeChildren(String name) {
			List<Entity> matches = getChildren(name);
			for(Entity target: matches) {
				children.remove(target);
			}
		}
		public void removeChildren() {
			children = new ArrayList<Entity>();
		}
		public boolean hasChildren() {
			if(children.size()==0) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	private Entity root;
	
	public EntityTree(String name) {
		root = new Entity(name);
	}
	
	public EntityTree(Entity root) {
		this.root = root;
	}
	
	public Entity addChild(String name) {
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

	public Entity getChild(String name) {
		return root.getChild(name);
	}

	public List<Entity> getChildren(String name) {
		return root.getChildren(name);
	}

	public List<Entity> getChildren() {
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

	public Entity getRoot() {
		return root;
	}
	
}
