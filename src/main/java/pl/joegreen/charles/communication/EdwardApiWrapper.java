package pl.joegreen.charles.communication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.joegreen.charles.configuration.EdwardApiConfiguration;
import pl.joegreen.edward.core.model.Project;
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
		return restClient.getTaskResultBlocking(taskId,
				RESULT_CHECK_INTERVAL_MS);
	}

	public long createProjectAndGetId(String projectName) {
		try {
			return restClient.addProject(projectName).getId();
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

	public Optional<String> getTaskResultIfDone(Long taskId)
			throws RestException {
		return restClient.getTaskResultIfDone(taskId);
	}

	public void abortTasks(List<Long> identifiers) throws RestException {
		logger.info("Aborting tasks: " + identifiers);
		restClient.abortTasks(identifiers);
	}

	public List<Long> addTasks(long jobId, List<Map<Object, Object>> tasks,
			long priority, long concurrentExecutions, long timeout) {
		try {
			return restClient.addTasks(jobId,
					objectMapper.writeValueAsString(tasks), priority,
					concurrentExecutions, timeout);
		} catch (JsonProcessingException | RestException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Optional<String>> getTasksResultsIfDone(List<Long> identifiers)
			throws RestException {
		return restClient.getTasksResultsIfDone(identifiers);
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
