package pl.joegreen.charles.configuration.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ValidationResult {

	private boolean isValid = true;

	public boolean isValid() {
		return isValid;
	}

	public List<ValidationError> errors = new ArrayList<ValidationError>();

	public void addError(String message) {
		errors.add(new ValidationError(message));
		isValid = false;
	}

	public ValidationResult addAnother(ValidationResult validationResult) {
		this.isValid = this.isValid && validationResult.isValid;
		this.errors.addAll(validationResult.errors);
		return this;
	}

	@Override
	public String toString() {
		if (isValid) {
			return "Validation result: valid";
		} else {
			return "Validation result: invalid, errors:\n"
					+ StringUtils.join(errors, "\n");
		}
	}
}