package pl.joegreen.charles;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptException;

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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

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
		if (!configuration.isAsynchronous()) {
			for (int i = 0; i < configuration.getMetaIterationsCount(); ++i) {
				logger.info("Performing meta iteration " + i);
				if (i > 0) {
					populations = migratePopulationsLocally(populations);
				}
				populations = improvePopulationsRemotely(populations);
			}
		} else {
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
		return populations;
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
		ImmutableList.Builder<Population> listBuilder = new Builder<Population>();
		for (int i = 0; i < configuration.getPopulationsCount(); ++i) {
			Map<Object, Object> phaseParameters = configuration
					.getPhaseConfiguration(PhaseType.GENERATE).getParameters();
			Population generatedPopulation = new Population(
					localExecutor.executeFunction(
							PhaseType.GENERATE.toFunctionName(),
							phaseParameters));
			listBuilder.add(generatedPopulation);
		}
		return listBuilder.build();
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
		ArrayList<Map<Object, Object>> arguments = populations
				.stream()
				.map(population -> {
					return addOptionsToArgument(
							population.getMapRepresentation(), "population",
							PhaseType.IMPROVE);
				}).collect(Collectors.toCollection(ArrayList::new));
		return edwardApiWrapper.addTasks(phaseJobIds.get(PhaseType.IMPROVE),
				arguments, configuration.getPriority(),
				configuration.getConcurrentExecutions());
	}

	private List<Population> getImprovedPopulations(List<Long> taskIdentifiers,
			List<Population> oldPopulations) throws RestException,
			JsonParseException, JsonMappingException, IOException {
		logger.info("Waiting for improved populations");
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
		return ImmutableList.copyOf(taskIdentifiers.stream()
				.map(taskId -> results.get(taskId))
				.collect(Collectors.toCollection(ArrayList::new)));
	}

	private void retrieveImprovedPopulations(Collection<Long> taskIdentifiers,
			Map<Long, Population> results) throws RestException, IOException,
			JsonParseException, JsonMappingException {
		List<Long> identifiersToGet = taskIdentifiers.stream()
				.filter(taskId -> !results.containsKey(taskId))
				.collect(Collectors.toList());
		List<Optional<String>> intermediateResults = edwardApiWrapper
				.returnTaskResultsIfDone(identifiersToGet);
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

	private List<Population> migratePopulationsLocally(
			Collection<Population> populations)
			throws CannotExecuteFunctionException {
		logger.info("Migrating populations locally");
		Collection<Map<Object, Object>> representations = populations.stream()
				.map(Population::getMapRepresentation)
				.collect(Collectors.toList());
		Map<Object, Object> argument = addOptionsToArgument(representations,
				"populations", PhaseType.MIGRATE);
		Map<Object, Object> result = localExecutor.executeFunction(
				PhaseType.MIGRATE.toFunctionName(), argument);
		List<Map<Object, Object>> newPopulations = (List<Map<Object, Object>>) result
				.get("populations");
		return newPopulations.stream().map(asMap -> new Population(asMap))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	public static void printAsPrettyJson(Map<Object, Object> map) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			logger.info(mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(map));
		} catch (IOException ex) {
			logger.error("Cannot parse json: " + map);
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

		ExperimentConfiguration experimentConfiguration = ExperimentConfiguration
				.fromFile(experimentConfigurationFilePath);

		ValidationResult experimentConfigurationValidationResult = experimentConfiguration
				.isValid();

		if (!experimentConfigurationValidationResult.isValid()) {
			throw new RuntimeException(
					experimentConfigurationValidationResult.toString()
							+ " \n Received configuration: \n"
							+ experimentConfiguration.toString());
		}

		EdwardApiConfiguration apiConfiguration = EdwardApiConfiguration
				.fromFile(apiConfigurationFilePath);

		ValidationResult apiConfigurationValidationResult = apiConfiguration
				.isValid();

		if (!apiConfigurationValidationResult.isValid()) {
			throw new RuntimeException(
					apiConfigurationValidationResult.toString()
							+ " \n Received configuration: \n"
							+ apiConfiguration.toString());
		}

		ArrayList<Long> times = new ArrayList<Long>();
		for (int i = 0; i < experimentConfiguration
				.getNumberOfExperimentRounds(); ++i) {
			long startTime = System.currentTimeMillis();
			IntStream
					.range(0,
							experimentConfiguration
									.getNumberOfParallelExperimentsInRound())
					.parallel()
					.forEach(
							number -> {
								Charles charles = new Charles(apiConfiguration,
										experimentConfiguration);
								try {
									List<Population> populations = charles
											.calculate();

									for (int populationNumber = 0; populationNumber < populations
											.size(); ++populationNumber) {
										logger.info("--- Population "
												+ populationNumber + " ---");
										printAsPrettyJson(populations.get(
												populationNumber)
												.getMapRepresentation());
									}
								} catch (Exception e) {
									throw new RuntimeException(e);
								}
							});
			Long time = System.currentTimeMillis() - startTime;
			times.add(time);
			logger.info("Time: " + time + " ms");
		}
		logger.info("All times: " + times);
	}
}
