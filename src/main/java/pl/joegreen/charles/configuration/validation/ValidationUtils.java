package pl.joegreen.charles.configuration.validation;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class ValidationUtils {
	public static ValidationResult checkNoNulls(
			Map<String, Object> descriptionsToObjects) {
		return checkPredicate(descriptionsToObjects, o -> o != null,
				desc -> desc + " cannot be null");
	}

	public static <T> ValidationResult checkPredicate(
			Map<String, T> descriptionsToObjects, Predicate<T> predicate,
			Function<String, String> descriptionToError) {
		ValidationResult result = new ValidationResult();
		for (String description : descriptionsToObjects.keySet()) {
			if (!predicate.test(descriptionsToObjects.get(description))) {
				result.addError(descriptionToError.apply(description));
			}
		}
		return result;
	}
}
