package pl.joegreen.charles.executor;

import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.joegreen.charles.executor.exception.CannotExecuteFunctionException;
import pl.joegreen.charles.executor.exception.CannotInitializeExecutorException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LocalExecutor {
	private final static Logger logger = LoggerFactory
			.getLogger(LocalExecutor.class);
	private final static ScriptEngineManager manager = new ScriptEngineManager();

	private final ScriptEngine engine = manager.getEngineByName("nashorn");

	public void initializeFunctionCode(String functionName, String functionCode)
			throws CannotInitializeExecutorException {
		try {
			CompiledScript compiled = ((Compilable) engine)
					.compile(functionCode);
			logger.debug("Creating function {} with code: {}", functionName,
					functionCode);
			engine.put(functionName, compiled.eval());
		} catch (Exception ex) {
			throw new CannotInitializeExecutorException(ex);
		}
	}

	public Map<Object, Object> executeFunction(String functionName,
			Map<Object, Object> input) throws CannotExecuteFunctionException {
		try {
			String inputAsString = new ObjectMapper().writeValueAsString(input);
			logger.debug("Setting 'input' variable in script engine to: {}",
					input);
			engine.put("input", inputAsString);
			engine.eval("input = JSON.parse(input)");
			String functionCall = functionName + "(input)";
			logger.debug("Calling function {}", functionCall);
			String result = (String) engine.eval("JSON.stringify("
					+ functionCall + ")");
			return new ObjectMapper().readValue(result, Map.class);
		} catch (Exception ex) {
			throw new CannotExecuteFunctionException(ex);
		}
	}
}
