package org.one.stone.soup.core.javascript;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JavascriptEngine {

	private ScriptEngine engine;
	
	public JavascriptEngine() {
		ScriptEngineManager factory = new ScriptEngineManager();
		engine = factory.getEngineByName("JavaScript");
	}
	
	public Object runScript(String script) throws ScriptException {
		return engine.eval(script);
	}
	
	public Object runScript(String script,String fileName) throws ScriptException {
		String currentFileName = (String) engine.get(ScriptEngine.FILENAME);
		try{
			engine.put(ScriptEngine.FILENAME,fileName);
			return engine.eval(script);
		} finally {
			engine.put(ScriptEngine.FILENAME,currentFileName);
		}
	}
	
	public void mount(String alias,Object api) {
		engine.put(alias, api);
	}
	
	public String[] getObjects() {
		//Bindings bindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);

		Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		
		return bindings.keySet().toArray(new String[]{});
	}
}
