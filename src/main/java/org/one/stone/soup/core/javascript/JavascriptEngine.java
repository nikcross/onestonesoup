package org.one.stone.soup.core.javascript;

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
	
	public void mount(String alias,Object api) {
		engine.put(alias, api);
	}
}
