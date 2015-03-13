package pl.joegreen.charles.configuration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pl.joegreen.charles.PhaseType;
import pl.joegreen.charles.configuration.validation.ValidationResult;
import pl.joegreen.charles.configuration.validation.ValidationUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;

public class ExperimentConfiguration {
	protected final static ObjectMapper JSON_MAPPER = new ObjectMapper()
			.configure(Feature.ALLOW_COMMENTS, true)
			.configure(Feature.ALLOW_SINGLE_QUOTES, true)
			.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

	private Integer numberOfExperimentRounds = 1;
	private Integer numberOfParallelExperimentsInRound = 1;
	private Integer metaIterationsCount;
	private Integer populationsCount;
	private Integer maxMetaIterationTime;
	private Integer priority = 0;
	private Integer concurrentExecutions = 1;
	private File source;
	private Boolean asynchronous;
	private Boolean printPopulations = true;

	private PhaseParameters generatePhase;
	private PhaseParameters improvePhase;
	private PhaseParameters migratePhase;

	private Map<String, Object> getDescriptionToFieldsMap() {
		Map<String, Object> builder = new HashMap<String, Object>();
		builder.put("asynchronous", asynchronous);
		builder.put("numberOfExperimentRounds", numberOfExperimentRounds);
		builder.put("numberOfParallelExperimentsInRound",
				numberOfParallelExperimentsInRound);
		builder.put("populationsCount", populationsCount);
		builder.put("metaIterationsCount", metaIterationsCount);
		builder.put("populationsCount", populationsCount);
		builder.put("metaIterationTime", metaIterationsCount);
		builder.put("generatePhase", generatePhase);
		builder.put("improvePhase", improvePhase);
		builder.put("migratePhase", migratePhase);
		builder.put("concurrentExecutions", concurrentExecutions);
		builder.put("priority", priority);
		builder.put("printPopulations", printPopulations);
		return Collections.unmodifiableMap(builder);
	}

	private ExperimentConfiguration() {

	}

	public PhaseParameters getPhaseConfiguration(PhaseType type) {
		switch (type) {
		case GENERATE:
			return generatePhase;
		case IMPROVE:
			return improvePhase;
		case MIGRATE:
			return migratePhase;
		default:
			throw new IllegalArgumentException("Unknown phase: " + type);

		}
	}

	public static ExperimentConfiguration fromFile(String path)
			throws JsonParseException, JsonMappingException, IOException {
		return fromFile(new File(path));
	}

	public static ExperimentConfiguration fromFile(File file)
			throws JsonParseException, JsonMappingException, IOException {
		ExperimentConfiguration configuration = JSON_MAPPER.readValue(file,
				ExperimentConfiguration.class);
		configuration.source = file;
		return configuration;
	}

	public ValidationResult isValid() {
		ValidationResult result = areNotNulls();
		result.addAnother(areNumberOptionsPositive());
		result.addAnother(generatePhase.isValid());
		result.addAnother(improvePhase.isValid());
		result.addAnother(migratePhase.isValid());
		return result;
	}

	private ValidationResult areNotNulls() {
		return ValidationUtils.checkNoNulls(getDescriptionToFieldsMap());
	}

	private ValidationResult areNumberOptionsPositive() {
		// priority doesn't have to be positive
		Set<Integer> fieldsToCheck = ImmutableSet.of(metaIterationsCount,
				populationsCount, maxMetaIterationTime, concurrentExecutions,
				numberOfExperimentRounds, numberOfParallelExperimentsInRound);
		Map<String, Integer> descriptionToInteger = new HashMap<String, Integer>();
		getDescriptionToFieldsMap().forEach((desc, val) -> {
			if (fieldsToCheck.contains(val)) {
				descriptionToInteger.put(desc, (Integer) val);
			}
		});

		return ValidationUtils.checkPredicate(descriptionToInteger, i -> i > 0,
				desc -> desc + " has to be >0.");
	}

	@Override
	public String toString() {
		return "ExperimentConfiguration [metaIterationsCount="
				+ metaIterationsCount + ", populationsCount="
				+ populationsCount + ", maxMetaIterationTime="
				+ maxMetaIterationTime + ", priority=" + priority
				+ ", concurrentExecutions=" + concurrentExecutions
				+ ", source=" + source + ", asynchronous=" + asynchronous
				+ ", generatePhase=" + generatePhase + ", improvePhase="
				+ improvePhase + ", migratePhase=" + migratePhase + "]";
	}

	public Integer getNumberOfExperimentRounds() {
		return numberOfExperimentRounds;
	}

	public Integer getNumberOfParallelExperimentsInRound() {
		return numberOfParallelExperimentsInRound;
	}

	public Integer getMetaIterationsCount() {
		return metaIterationsCount;
	}

	public Integer getPopulationsCount() {
		return populationsCount;
	}

	public Integer getMaxMetaIterationTime() {
		return maxMetaIterationTime;
	}

	public Integer getPriority() {
		return priority;
	}

	public Integer getConcurrentExecutions() {
		return concurrentExecutions;
	}

	public File getSource() {
		return source;
	}

	public Boolean isAsynchronous() {
		return asynchronous;
	}

	public PhaseParameters getGeneratePhase() {
		return generatePhase;
	}

	public PhaseParameters getImprovePhase() {
		return improvePhase;
	}

	public PhaseParameters getMigratePhase() {
		return migratePhase;
	}

	public Boolean isPrintPopulations() {
		return printPopulations;
	}

}
