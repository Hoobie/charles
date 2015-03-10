package pl.joegreen.charles;

public enum PhaseType {
	GENERATE, IMPROVE, MIGRATE;

	public String toFunctionName() {
		return toString().toLowerCase();
	}
}
