package pl.joegreen.charles.configuration;

import java.util.List;
import java.util.Map;

import pl.joegreen.charles.configuration.validation.ValidationResult;

import com.google.common.collect.ImmutableMap;

public class PhaseParameters {
	private List<String> codeFiles;
	private Map<Object, Object> parameters;

	public ValidationResult isValid() {
		ValidationResult result = new ValidationResult();
		if (codeFiles == null || codeFiles.isEmpty()) {
			result.addError("codeFiles cannot be null or empty in any phase");
		}
		return result;
	}

	@Override
	public String toString() {
		return "PhaseParameters [codeFiles=" + codeFiles + ", parameters="
				+ parameters + "]";
	}

	public List<String> getCodeFiles() {
		return codeFiles;
	}

	public Map<Object, Object> getParameters() {
		return parameters != null ? parameters : ImmutableMap.of();
	}
}
