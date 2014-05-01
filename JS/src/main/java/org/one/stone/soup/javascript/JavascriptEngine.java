package org.one.stone.soup.javascript;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.one.stone.soup.core.FileHelper;

public class JavascriptEngine {

	public static final String GLOBAL = "global";

	private static final class JsContext extends Context {
		long startTime;
		long totalInstructions = 0;
		boolean stop = false;
		Scriptable scriptable;

		JsContext(ContextFactory contextFactory) {
			super(contextFactory);
		}

		public void stop() {
			stop = true;
		}
	}

	class JsContextFactory extends ContextFactory {
		JsContext createContext() {
			JsContext context = new JsContext(this);
			context.setOptimizationLevel(-1);
			context.setInstructionObserverThreshold(1000);

			return context;
		}

		@Override
		protected void observeInstructionCount(Context context,
				int instructionCount) {
			((JsContext) context).totalInstructions += instructionCount;
			// System.out.println("inst:"+((JsContext)context).totalInstructions);

			if (((JsContext) context).stop == true) {
				throw new Error("Stop Requested");
			}
			if (System.currentTimeMillis() - ((JsContext) context).startTime > maximumScriptTime) {
				throw new Error("Too long");
			}
			if (((JsContext) context).totalInstructions > maximumScriptInstructions) {
				throw new Error("Too many instructions");
			}
		}
	}

	class ScriptParameter {
		String name;
		Object value;
	}
	
	private static JavascriptEngine instance;
	
	private long maximumScriptTime = 10000;
	private int maximumScriptInstructions = 1000000000;
	private JsContextFactory contextFactory = new JsContextFactory();
	private static Map<String,JsContext> scopes = new HashMap<String,JsContext>();
	
	private JavascriptEngine() {
	}
	
	public static JavascriptEngine getInstance() {
		if(instance==null) {
			instance = new JavascriptEngine();
			instance.initialiseEngine();
		}
		return instance;
	}
	
	private void initialiseEngine() {
		JsContext global = getScope(GLOBAL);
	}
	
	private JsContext getScope(String scopeName) {
		JsContext context = scopes.get(scopeName);
		if(context!=null) {
			return context;
		}
		context = contextFactory.createContext();
		if(scopeName.equals(GLOBAL)) {
			Scriptable scritable = context.initStandardObjects();
			context.scriptable = scritable;
		} else {
			Scriptable scritable = context.newObject(scopes.get(GLOBAL).scriptable);
			context.scriptable = scritable;
		}

		scopes.put(scopeName,context);
		return context;
	}
	
	public JavascriptEngine getEngine(String scope) {
		JavascriptEngine engine = new JavascriptEngine();
		engine.getScope(scope);
		return engine;
	}
	
	public void mount(String alias, Object instance) {
		mount(GLOBAL,alias,instance);
	}
	
	public void mount(String scope,String alias, Object instance) {
		Scriptable scriptable = getScope(scope).scriptable;
		Context.enter();
		Object wrappedOut = Context.toObject(instance, scriptable);
		ScriptableObject.putProperty(scriptable, alias, wrappedOut);
		Context.exit();
	}

	public Object runScript(String command) throws JavascriptException {
		try{
			Context.enter();
			return runScript(command,"Command");
		} finally {
			Context.exit();
		}
	}

	public Object runScriptFromFile(String loadFileAsString, String fileName) throws JavascriptException, IOException {
		String script = FileHelper.loadFileAsString(new File(fileName) );
		return runScript(script, fileName);
	}

	public Object runScript(String script,String scriptName) throws JavascriptException {
		return runScript(GLOBAL,script,scriptName);
	}
	
	public Object runScript(String scope,String script,String scriptName) throws JavascriptException {
		try{
			Context.enter();
			JsContext context = getScope(scope);
			context.startTime = System.currentTimeMillis();
			Scriptable scriptable = context.scriptable;
			
			try {
				Object result = context.evaluateString(scriptable, script, scriptName, 0, null);
				return result;
			} catch (EcmaError error) {
				throw new JavascriptException(error.details() + "\n\n"
						+ error.getScriptStackTrace()+ "\n\n"
						+ "in "+ scriptName
						, error.lineNumber() - 1,
						error.columnNumber());
			} catch (EvaluatorException evaluatorException) {
				throw new JavascriptException(evaluatorException.details() + "\n\n"
						+ evaluatorException.getScriptStackTrace(),
						evaluatorException.lineNumber() - 1,
						evaluatorException.columnNumber());
			}
		} finally {
			Context.exit();
		}
	}
	
	public String[] getObjects() {
		Object[] keyObjs = scopes.get(GLOBAL).scriptable.getIds();
		String[] keys = new String[keyObjs.length];
		for(int i=0;i<keys.length;i++) {
			keys[i] = (String)keyObjs[i];
		}
		return keys;
	}

	public String getObjectKey(Object object) {
		Scriptable scriptable = scopes.get(GLOBAL).scriptable;
		String[] keys = getObjects();
		for(String key: keys) {
			if(scriptable.get(key, scriptable)==object) {
				return key;
			}
		}
		return null;
	}

	public Object getObject(String name) {
		Scriptable scriptable = scopes.get(GLOBAL).scriptable;
		return scriptable.get(name, scriptable);
	}
}
