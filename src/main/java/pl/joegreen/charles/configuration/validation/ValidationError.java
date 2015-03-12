package pl.joegreen.charles.configuration.validation;

public class ValidationError {
	private String message;

	public String getMessage() {
		return message;
	}

	public ValidationError(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return message;
	}

}