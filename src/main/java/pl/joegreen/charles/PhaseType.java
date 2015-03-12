package pl.joegreen.charles;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PhaseType {
	GENERATE(false), IMPROVE(true), MIGRATE(false);

	private final boolean isRemote;

	private PhaseType(boolean isRemote) {
		this.isRemote = isRemote;
	}

	public boolean isRemote() {
		return this.isRemote;
	}

	public boolean isLocal() {
		return !this.isRemote;
	}

	public String toFunctionName() {
		return toString().toLowerCase();
	}

	public static List<PhaseType> getRemotePhases() {
		return Arrays.asList(values()).stream().filter(PhaseType::isRemote)
				.collect(Collectors.toList());
	}

	public static List<PhaseType> getLocalPhases() {
		return Arrays.asList(values()).stream().filter(PhaseType::isLocal)
				.collect(Collectors.toList());
	}

}
