package org.onestonesoup.core.data;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.onestonesoup.core.NameHelper;

public class JavaTree {

	public static EntityTree toEntityTree(Object instance) {
		return new EntityTree(toTree(instance));
	}
	
	public static EntityTree.TreeEntity toTree(Object instance)	{
		if(instance==null)
		{
			instance = new NullPointerException();
		}
		
		EntityTree xInstance = new EntityTree("instance");

		Class clazz = instance.getClass();
		String className = clazz.getName();
		xInstance.setAttribute("class",className);

		Method[] getters = getGetters(clazz);

		for(int loop=0;loop<getters.length;loop++)
		{
			try{
				String returnTypeClassName = getters[loop].getReturnType().getName();
				Object attribute = getters[loop].invoke(instance,new Object[]{});

				if(attribute==null)
				{
					continue;
				}

				String attributeName = getters[loop].getName().substring(3);
				attributeName = NameHelper.classToTitleName( attributeName );
				attributeName = NameHelper.titleToDataName( attributeName );

				EntityTree.TreeEntity xAttribute = xInstance.addChild("attribute");
				xAttribute.setAttribute( "class",returnTypeClassName );
				xAttribute.setAttribute( "name",attributeName );

				if(
						attribute instanceof String
				)
				{
					xAttribute.setValue( (String)attribute );
				}
				else if(
						attribute instanceof String[]
				)
				{
					int count = Array.getLength(attribute);
					for(int loopA=0;loopA<count;loopA++)
					{
						Object item = Array.get(attribute,loopA);
						EntityTree.TreeEntity xItem = xAttribute.addChild("item");

						xItem.setValue( (String)item );
					}
				}				
				else if(
						attribute instanceof Boolean ||
						attribute instanceof Integer ||
						attribute instanceof Long ||
						attribute instanceof Double
				)
				{
					xAttribute.setAttribute( "value",""+attribute );
				}
				else if(
						attribute instanceof Boolean[] ||
						attribute instanceof Integer[] ||
						attribute instanceof Long[] ||
						attribute instanceof Double[]
				)
				{
					int count = Array.getLength(attribute);
					for(int loopA=0;loopA<count;loopA++)
					{
						Object item = Array.get(attribute,loopA);
						EntityTree.TreeEntity xItem = xAttribute.addChild("item");

						xItem.setAttribute( "value",""+item );
					}
				}
				else if(attribute.getClass().isArray())
				{
					int count = Array.getLength(attribute);
					for(int loopA=0;loopA<count;loopA++)
					{
						Object item = Array.get(attribute,loopA);
						xAttribute.addChild( toTree(item) );
					}
				}
				else
				{
					xAttribute.addChild( toTree(attribute) );
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		return xInstance.getRoot();
	}

	private static Method[] getGetters(Class clazz)
	{
		List<Method> getters = new ArrayList<Method>();
		for(Method method: clazz.getMethods())
		{
			if(
					method.getName().substring(0,3).equals("get") &&
					method.getParameterTypes().length==0 &&
					Modifier.isPublic( method.getModifiers() )==true)
			{
				getters.add(method);
			}
		}

		// Check for matching setter
		for(Method method: clazz.getMethods())
		{
			Class returnType = method.getReturnType();
			String setterName = "s"+method.getName().substring(1);
			try{
				Method setter = clazz.getMethod(setterName,new Class[]{returnType});
				if( Modifier.isPublic(setter.getModifiers())==false )
				{
					getters.remove(method);	
				}
			}
			catch(NoSuchMethodException me)
			{
				getters.remove(method);
			}
		}
		return getters.toArray(new Method[]{});
	}
	
	public static Object toObject(EntityTree xInstance) {
		return toObject(xInstance.getRoot());
	}
	
	public static Object toObject(EntityTree.TreeEntity xInstance) {
		try{
			String className = xInstance.getAttribute("class");
			Class clazz = Class.forName(className);
			Object instance = clazz.newInstance();

			List<EntityTree.TreeEntity> xAttributes = xInstance.getChildren("attribute");

			for(int loop=0;loop<xAttributes.size();loop++)
			{

				EntityTree.TreeEntity xAttribute = xAttributes.get(loop);

				String attributeName = xAttribute.getAttribute("name");
				String setterName = NameHelper.dataToTitleName( attributeName );
				setterName = NameHelper.titleToClassName( setterName );
				setterName = "set"+setterName;

				String attributeClassName = xAttribute.getAttribute("class");

				Class attributeClass = getClassForName(attributeClassName);

				String value = xAttribute.getAttribute("value");

				Method setter = clazz.getMethod(setterName,new Class[]{attributeClass} );
				if( Modifier.isPublic(setter.getModifiers())==false )
				{
					continue;
				}
				
				Object attribute = null;

				if(attributeClass.isArray())
				{
					if(attributeClass.isPrimitive() || attributeClass==String[].class)
					{
						attribute = Array.newInstance( attributeClass.getComponentType(),xAttribute.getChildren().size());
						for(int loopI=0;loopI<xAttribute.getChildren().size();loopI++)
						{
							value = xAttribute.getChildren().get(loopI).getAttribute("value");

							Object item = null;

							if( attributeClass == String[].class )
							{
								item = xAttribute.getChildren().get(loopI).getValue();
							}
							else if( attributeClass == Boolean[].class || attributeClass == boolean[].class )
							{
								item = new Boolean(value);
							}
							else if( attributeClass == Integer.class || attributeClass == int[].class )
							{
								item = new Integer(value);
							}
							else if( attributeClass == Long.class || attributeClass == long[].class )
							{
								item = new Long(value);
							}
							else if( attributeClass == Double.class || attributeClass == double[].class )
							{
								item = new Double(value);
							}

							Array.set(attribute,loopI,item);
						}
					}
					else
					{
						attribute = Array.newInstance(attributeClass.getComponentType(),xAttribute.getChildren().size());
						for(int loopI=0;loopI<xAttribute.getChildren().size();loopI++)
						{
							Object item = toObject( xAttribute.getChildren().get(loopI) );

							Array.set(attribute,loopI,item);
						}
					}
				}
				else if(attributeClass == String.class || xAttribute.getChildren().size()==0)
				{
					if( attributeClass == String.class )
					{
						attribute = xAttribute.getValue();
					}
					else if( attributeClass == Boolean.class || attributeClass == boolean.class )
					{
						attribute = new Boolean(value);
					}
					else if( attributeClass == Integer.class || attributeClass == int.class )
					{
						attribute = new Integer(value);
					}
					else if( attributeClass == Long.class || attributeClass == long.class )
					{
						attribute = new Long(value);
					}
					else if( attributeClass == Double.class || attributeClass == double.class )
					{
						attribute = new Double(value);
					}
				}
				else
				{
					attribute = toObject( xAttribute.getChildren().get(0) );
				}

				setter.invoke( instance,new Object[]{attribute} );
			}

			return instance;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private static Class getClassForName(String className)
	{
		Class attributeClass = null;

		try{
			if(className.equals("int"))
			{
				attributeClass = int.class;
			}
			else if(className.equals("double"))
			{
				attributeClass = double.class;
			}
			else if(className.equals("boolean"))
			{
				attributeClass = boolean.class;
			}
			else if(className.equals("long"))
			{
				attributeClass = long.class;
			}
			else
			{
				attributeClass = Class.forName( className );
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return attributeClass;
	}
}