package pl.joegreen.charles;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.joegreen.charles.communication.EdwardApiWrapper;
import pl.joegreen.charles.configuration.CodeReader;
import pl.joegreen.charles.configuration.EdwardApiConfiguration;
import pl.joegreen.charles.configuration.ExperimentConfiguration;
import pl.joegreen.charles.configuration.validation.ValidationResult;
import pl.joegreen.charles.executor.LocalExecutor;
import pl.joegreen.charles.executor.exception.CannotExecuteFunctionException;
import pl.joegreen.charles.executor.exception.CannotInitializeExecutorException;
import pl.joegreen.edward.rest.client.RestException;

import javax.script.ScriptException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Charles {

	private static final String META_ITERATION_NUMBER_PROPERTY = "metaIteration";
	private static final String PROJECT_NAME = "Charles";
	private final static Logger logger = LoggerFactory.getLogger(Charles.class);
	private final ExperimentConfiguration configuration;

	private EdwardApiWrapper edwardApiWrapper;

	private Map<PhaseType, String> phaseCodes;

	private LocalExecutor localExecutor = new LocalExecutor();

	private Long projectId;
	private Map<PhaseType, Long> phaseJobIds;

	private ObjectMapper objectMapper = new ObjectMapper();

	public Charles(EdwardApiConfiguration apiConfiguration,
			ExperimentConfiguration configuration) {
		this.edwardApiWrapper = new EdwardApiWrapper(apiConfiguration);
		this.configuration = configuration;
		String generateCode = CodeReader.readCode(configuration,
                PhaseType.GENERATE);
		String improveCode = CodeReader.readCode(configuration,
				PhaseType.IMPROVE);
		String migrateCode = CodeReader.readCode(configuration,
				PhaseType.MIGRATE);
		phaseCodes = ImmutableMap.of(PhaseType.GENERATE, generateCode,
				PhaseType.IMPROVE, improveCode, PhaseType.MIGRATE, migrateCode);
	}

	public List<Population> calculate() throws CannotExecuteFunctionException,
			JsonProcessingException, RestException, IOException,
			CannotInitializeExecutorException {
		initializeLocalJavaScriptEngine();
		initializeVolunteerComputingJobs();

		List<Population> populations = generatePopulationsLocally();
		if(logger.isTraceEnabled()) {
			logger.trace("Generated populations: \n {} ", populationsToString(populations));
		}
		if (!configuration.isAsynchronous()) {
			populations = improveAndMigrateSynchronously(populations);
		} else {
			improveAndMigrateAsynchronously(populations);
		}
		return populations;
	}

	private List<Population> improveAndMigrateSynchronously(List<Population> populations)
			throws CannotExecuteFunctionException, RestException, IOException {

		for (int i = 0; i < configuration.getMetaIterationsCount(); ++i) {
            logger.info("Performing meta iteration " + i);
            long volunteersPopulationsDelta = Math.abs(edwardApiWrapper.getVolunteersCount() - populations.size());
            if (volunteersPopulationsDelta > 0) {
                for (int j = 0; j < volunteersPopulationsDelta; j++) {
                    Map<Object, Object> phaseParameters = configuration
                            .getPhaseConfiguration(PhaseType.GENERATE).getParameters();
                    Population generatedPopulation = new Population(
                            localExecutor.executeFunction(
                                    PhaseType.GENERATE.toFunctionName(),
                                    phaseParameters));
					logger.info("Adding {} population(s)", volunteersPopulationsDelta);
					populations.add(generatedPopulation);
                }
            } else {
				for (int j = 0; j < volunteersPopulationsDelta && populations.size() > 0; j++) {
					logger.info("Removing {} population(s)", volunteersPopulationsDelta);
					populations.remove(populations.size() - 1);
                }
            }

            if (i > 0) {
				populations = migratePopulationsLocally(populations);
			}
			populations = improvePopulationsRemotely(populations);
            if (logger.isTraceEnabled()) {
                logger.trace("Improved populations: \n {} ", populationsToString(populations));
            }
        }
		return populations;
	}

	private void improveAndMigrateAsynchronously(List<Population> populations) throws RestException, IOException {
		populations.forEach(population -> population.put(
                META_ITERATION_NUMBER_PROPERTY, 0));
		Set<Long> remoteTasks = new HashSet<Long>();

		List<Population> finalResults = new ArrayList<Population>();

		Population migrationPool = new Population(
                new HashMap<Object, Object>());
		migrationPool.put("individuals", new ArrayList<Object>());

		List<Long> taskIdentifiers = sendPopulationsToVolunteers(populations);
		remoteTasks.addAll(taskIdentifiers);

		while (finalResults.size() < populations.size()) {

            Map<Long, Population> results = new HashMap<Long, Population>();
            retrieveImprovedPopulations(remoteTasks, results);
            results.values()
                    .stream()
                    .forEach(
							population -> population
									.put(META_ITERATION_NUMBER_PROPERTY,
                                            ((Integer) population
                                                    .get(META_ITERATION_NUMBER_PROPERTY)) + 1));

            results.values()
                    .forEach(
							population -> {
								if (((Integer) population
										.get(META_ITERATION_NUMBER_PROPERTY))
										.equals(configuration
                                                .getMetaIterationsCount())) {
									finalResults.add(population);
								} else {
									List<Long> identifiers = sendPopulationsToVolunteers(Arrays
											.asList(migratePopulationLocallyAsynchronously(
                                                    population,
                                                    migrationPool)));
									remoteTasks.add(identifiers.get(0));
								}

							});
            remoteTasks.removeAll(results.keySet());
        }
	}


	private void initializeLocalJavaScriptEngine()
			throws CannotInitializeExecutorException {
		for (PhaseType phaseType : PhaseType.getLocalPhases()) {
			localExecutor.initializeFunctionCode(phaseType.toFunctionName(),
					phaseCodes.get(phaseType));
		}
	}

	private Population migratePopulationLocallyAsynchronously(
			Population population, Population pool) {
		logger.info("Migrating population locally");
		Map<Object, Object> argument = addOptionsToArgument(
                population.getMapRepresentation(), "population",
                PhaseType.MIGRATE);
		argument.put("pool", pool.getMapRepresentation());
		try {
			Map<Object, Object> migrationResult = localExecutor
					.executeFunction(PhaseType.MIGRATE.toFunctionName(),
                            argument);
			pool.put("individuals", ((Map<Object, Object>) migrationResult
					.get("migrationPool")).get("individuals"));
			Population populationAfterMigration = new Population(
					(Map<Object, Object>) migrationResult.get("population"));
			return populationAfterMigration;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private void initializeVolunteerComputingJobs() {
		projectId = edwardApiWrapper.findProjectWithName(PROJECT_NAME).orElse(
				null);
		if (projectId == null) {
			projectId = edwardApiWrapper.createProjectAndGetId(PROJECT_NAME);
		}

		String jobNameSuffix = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss")
				.format(new Date());
		phaseJobIds = new HashMap<PhaseType, Long>();
		for (PhaseType phaseType : PhaseType.getRemotePhases()) {
			Long jobId = edwardApiWrapper.createJobAndGetId(projectId,
					phaseType.toFunctionName() + jobNameSuffix,
					phaseCodes.get(phaseType));
			phaseJobIds.put(phaseType, jobId);
		}
	}

	private Map<Object, Object> addOptionsToArgument(Object argument,
			String argumentName, PhaseType phaseType) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put(argumentName, argument);
		map.put("parameters", configuration.getPhaseConfiguration(phaseType)
				.getParameters());
		return map;
	}

	private List<Population> generatePopulationsLocally()
			throws CannotExecuteFunctionException {
		logger.info("Generating populations locally");
		List<Population> populations = new ArrayList<>();
		for (int i = 0; i < edwardApiWrapper.getVolunteersCount(); ++i) {
			Map<Object, Object> phaseParameters = configuration
					.getPhaseConfiguration(PhaseType.GENERATE).getParameters();
			Population generatedPopulation = new Population(
					localExecutor.executeFunction(
							PhaseType.GENERATE.toFunctionName(),
							phaseParameters));
			populations.add(generatedPopulation);
		}
		return populations;
	}

	private List<Population> improvePopulationsRemotely(
			List<Population> populations) throws JsonProcessingException,
			RestException, IOException {
		List<Long> taskIdentifiers = sendPopulationsToVolunteers(populations);
		return getImprovedPopulations(taskIdentifiers, populations);
	}

	private List<Long> sendPopulationsToVolunteers(
			Collection<Population> populations) {
		logger.info(String.format(
				"Sending %d population improvements tasks to volunteers. ",
				populations.size()));
        long startTime = System.currentTimeMillis();
		ArrayList<Map<Object, Object>> arguments = populations
				.stream()
				.map(population -> {
                    return addOptionsToArgument(
                            population.getMapRepresentation(), "population",
                            PhaseType.IMPROVE);
                }).collect(Collectors.toCollection(ArrayList::new));
        List<Long> taskIdentifiers = edwardApiWrapper.addTasks(phaseJobIds.get(PhaseType.IMPROVE),
                arguments, configuration.getPriority(),
                configuration.getConcurrentExecutions(),
                configuration.getTimeout());
        logger.info("Sending tasks to volunteers took {} ms ", System.currentTimeMillis() - startTime);
        return taskIdentifiers;
    }

	private List<Population> getImprovedPopulations(List<Long> taskIdentifiers,
			List<Population> oldPopulations) throws RestException, IOException {
		logger.info("Waiting for improved populations");
        long startTime = System.currentTimeMillis();
		Map<Long, Population> results = new HashMap<Long, Population>();
		long waitingStartTime = System.currentTimeMillis();
		while (results.size() < taskIdentifiers.size()
				&& (System.currentTimeMillis() - waitingStartTime) < configuration
						.getMaxMetaIterationTime()) {
			retrieveImprovedPopulations(taskIdentifiers, results);
		}
		if (results.size() < taskIdentifiers.size()) {
			logger.info("Lacking " + (taskIdentifiers.size() - results.size())
					+ " improved populations after waiting "
					+ (System.currentTimeMillis() - waitingStartTime)
					+ " ms. Using old populations instead.");
			useOldPopulationWhereNoImprovedYet(taskIdentifiers, oldPopulations,
					results);
		}
        logger.info("Waiting for improved populations took {} ms ", System.currentTimeMillis() - startTime);
        return taskIdentifiers.stream()
				.map(results::get)
				.collect(Collectors.toList());
	}

	private void retrieveImprovedPopulations(Collection<Long> taskIdentifiers,
			Map<Long, Population> results) throws RestException, IOException, JsonMappingException {
		List<Long> identifiersToGet = taskIdentifiers.stream()
				.filter(taskId -> !results.containsKey(taskId))
				.collect(Collectors.toList());
        long startTime = System.currentTimeMillis();
        List<Optional<String>> intermediateResults = edwardApiWrapper
                .getTasksResultsIfDone(identifiersToGet);
        if (intermediateResults.stream().anyMatch(Optional::isPresent)) {
            logger.info("Loading {} results took {} ms", intermediateResults.stream().filter(Optional::isPresent).count(), System.currentTimeMillis() - startTime);
        }
        for (int i = 0; i < identifiersToGet.size(); ++i) {
			Long taskIdentifier = identifiersToGet.get(i);
			Optional<String> result = intermediateResults.get(i);
			if (result.isPresent()) {
				logger.info("Received improved population from task "
						+ taskIdentifier);
				results.put(
						taskIdentifier,
						new Population(objectMapper.readValue(result.get(),
								Map.class)));

			}
		}
	}

	private void useOldPopulationWhereNoImprovedYet(List<Long> taskIdentifiers,
			List<Population> oldPopulations, Map<Long, Population> results) {
		taskIdentifiers
				.stream()
				.filter(taskId -> !results.containsKey(taskId))
				.forEach(
                        taskId -> results.put(taskId, oldPopulations
                                .get(taskIdentifiers.indexOf(taskId))));
		try {
			edwardApiWrapper.abortTasks(taskIdentifiers);
		} catch (RestException e) {
			logger.warn("Cannot abort some tasks ", e);
			// do not stop execution
		}
	}

	private List<Population> migratePopulationsLocally(List<Population> populations)
			throws CannotExecuteFunctionException {
		logger.info("Migrating populations locally");
		long startTime = System.currentTimeMillis();

		Collection<Map<Object, Object>> representations = populations.stream()
				.map(Population::getMapRepresentation)
				.collect(Collectors.toList());

        logger.info("Populations before migrate: " + representations);

		Map<Object, Object> functionArguments;
		Map<Object, Object> functionResult;

		for (Map<Object, Object> firstPopulation : representations) {
			for (Map<Object, Object> secondPopulation : representations) {
				if (!firstPopulation.equals(secondPopulation)) {
					functionArguments = new HashMap<>();
					functionArguments.put("firstPopulation", firstPopulation);
					functionArguments.put("secondPopulation", secondPopulation);

					// Add config
					functionArguments.put("parameters", configuration.getPhaseConfiguration(PhaseType.MIGRATE)
							.getParameters());

					// Execute migrate.js
					functionResult = localExecutor.executeFunction(
							PhaseType.MIGRATE.toFunctionName(), functionArguments);

					// 'Parse' response
					firstPopulation.clear();
					firstPopulation.putAll((Map<Object, Object>) ((Map<Object, Object>) functionResult
							.get("populations")).get("firstPopulation"));
					secondPopulation.clear();
					secondPopulation.putAll((Map<Object, Object>) ((Map<Object, Object>) functionResult
							.get("populations")).get("secondPopulation"));
				}
			}
		}

        logger.info("Populations after migrate: " + representations);
		logger.info("Migrating populations locally took {} ms", (System.currentTimeMillis() - startTime));

        return populations;
	}

	public static String toPrettyJsonString(Map<Object, Object> map) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
					map);
		} catch (IOException ex) {
			logger.error("Cannot write as json: " + map);
			throw new IllegalArgumentException(ex);
		}
	}

	public static void main(String[] args) throws IOException, RestException,
			ScriptException {
		String experimentConfigurationFilePath = null;
		String apiConfigurationFilePath = null;
		if (args.length != 2) {
			System.err
					.println("arguments: apiConfigurationFilePath experimentConfigurationFilePath ");
			System.exit(-1);
		} else {
			apiConfigurationFilePath = args[0];
			experimentConfigurationFilePath = args[1];
		}

		System.out.println(performExperiment(apiConfigurationFilePath,
				experimentConfigurationFilePath));
	}

	public static String performExperiment(String apiConfigurationFilePath,
			String experimentConfigurationFilePath) throws JsonParseException,
			JsonMappingException, IOException {
		ExperimentConfiguration experimentConfiguration = ExperimentConfiguration
				.fromFile(experimentConfigurationFilePath);

		EdwardApiConfiguration apiConfiguration = EdwardApiConfiguration
				.fromFile(apiConfigurationFilePath);

		ValidationResult experimentConfigurationValidationResult = experimentConfiguration
				.isValid();

		if (!experimentConfigurationValidationResult.isValid()) {
			throw new RuntimeException(
					experimentConfigurationValidationResult.toString()
							+ " \n Received configuration: \n"
							+ experimentConfiguration.toString());
		}

		ValidationResult apiConfigurationValidationResult = apiConfiguration
				.isValid();

		if (!apiConfigurationValidationResult.isValid()) {
			throw new RuntimeException(
					apiConfigurationValidationResult.toString()
							+ " \n Received configuration: \n"
							+ apiConfiguration.toString());
		}

		ArrayList<Long> times = new ArrayList<Long>();
		Charles charles = new Charles(apiConfiguration, experimentConfiguration);
		try {
			List<Population> populations = charles.calculate();
			return populationsToString(populations);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public static String populationsToString(List<Population> populations) {
		StringBuilder builder = new StringBuilder();
		for (int populationNumber = 0; populationNumber < populations.size(); ++populationNumber) {
			builder.append("--- Population " + populationNumber + " ---");
			builder.append(toPrettyJsonString(populations.get(populationNumber)
					.getMapRepresentation()));
		}
		return builder.toString();
	}
}
