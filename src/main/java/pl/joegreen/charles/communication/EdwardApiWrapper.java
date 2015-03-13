package pl.joegreen.charles.communication;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.joegreen.charles.configuration.EdwardApiConfiguration;
import pl.joegreen.edward.core.model.JsonData;
import pl.joegreen.edward.core.model.Project;
import pl.joegreen.edward.core.model.TaskStatus;
import pl.joegreen.edward.rest.client.RestClient;
import pl.joegreen.edward.rest.client.RestException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EdwardApiWrapper {

	private final static int RESULT_CHECK_INTERVAL_MS = 100;
	private RestClient restClient;

	public EdwardApiWrapper(EdwardApiConfiguration apiProperties) {
		restClient = new RestClient(apiProperties.getUser(),
				apiProperties.getPassword(), apiProperties.getHostname(),
				apiProperties.getPort(), "http", apiProperties.getPrefix());
	}

	private final static Logger logger = LoggerFactory
			.getLogger(EdwardApiWrapper.class);

	private ObjectMapper objectMapper = new ObjectMapper();

	public String blockUntilResult(Long taskId) throws RestException {
		Optional<String> result;
		while (true) {
			result = returnTaskResultIfDone(taskId);
			if (result.isPresent()) {
				return result.get();
			} else {
				try {
					Thread.sleep(RESULT_CHECK_INTERVAL_MS);
				} catch (InterruptedException e) {
					throw new RuntimeException(e); // TODO:
				}
			}
		}
	}

	public long createProjectAndGetId(String projectName) {
		try {
			return restClient.addProject(projectName, 1L).getId();
		} catch (RestException e) {
			throw new RuntimeException(e);
		}
	}

	public long createJobAndGetId(long projectId, String jobName, String jobCode) {
		try {
			return restClient.addJob(projectId, jobName,
					"var compute = " + jobCode).getId();
		} catch (RestException e) {
			throw new RuntimeException(e);
		}
	}

	public Optional<String> returnTaskResultIfDone(Long taskId)
			throws RestException {
		return returnTaskResultsIfDone(Collections.singletonList(taskId))
				.get(0);
	}

	public void abortTasks(List<Long> identifiers) throws RestException {
		logger.info("Aborting tasks: " + identifiers);
		restClient.abortTasks(identifiers);
	}

	public List<Long> addTasks(long jobId, List<Map<Object, Object>> tasks,
			int priority, int concurrentExecutions) {
		try {
			return restClient.addTasks(jobId,
					objectMapper.writeValueAsString(tasks), priority,
					concurrentExecutions);
		} catch (JsonProcessingException | RestException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Optional<String>> returnTaskResultsIfDone(List<Long> identifiers)
			throws RestException {
		Map<Long, TaskStatus> idToStatus = restClient
				.getTasksStatuses(identifiers);
		List<Long> finishedTaskIdentifiers = identifiers.stream()
				.filter(id -> idToStatus.get(id).equals(TaskStatus.FINISHED))
				.collect(Collectors.toList());

		Map<Long, JsonData> idToResult = finishedTaskIdentifiers.isEmpty() ? null
				: restClient.getResults(finishedTaskIdentifiers);

		return identifiers
				.stream()
				.map(id -> {
					TaskStatus status = idToStatus.get(id);
					switch (status) {
					case ABORTED:
						throw new RuntimeException(
								"Computation cannot be finished because the task was aborted. Task id: "
										+ id);
					case FAILED:
						throw new RuntimeException(
								"Computation cannot be finished because the task failed. Task id: "
										+ id); // TODO: get failure message
					case IN_PROGRESS:
						return Optional.<String> ofNullable(null);
					case FINISHED:
						return Optional.<String> of(idToResult.get(id)
								.getData());
					default:
						throw new IllegalStateException();
					}
				}).collect(Collectors.toList());
	}

	public Optional<Long> findProjectWithName(String projectName) {
		try {
			List<Project> projects = restClient.getProjects();
			Optional<Project> projectWithName = projects.stream()
					.filter(project -> project.getName().equals(projectName))
					.findFirst();
			return projectWithName.map(project -> project.getId());
		} catch (RestException ex) {
			throw new RuntimeException(ex);
		}
	}
}