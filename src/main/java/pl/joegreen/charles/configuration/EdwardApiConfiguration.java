package pl.joegreen.charles.configuration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import pl.joegreen.charles.configuration.validation.ValidationResult;
import pl.joegreen.charles.configuration.validation.ValidationUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

public class EdwardApiConfiguration {
	protected final static ObjectMapper JSON_MAPPER = new ObjectMapper()
			.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
			.configure(Feature.ALLOW_COMMENTS, true)
			.configure(Feature.ALLOW_SINGLE_QUOTES, true)
			.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

	private String hostname;
	private int port;
	private String user;
	private String password;
	private String prefix;

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public ValidationResult isValid() {
		ValidationResult result;
		Map<String, String> descriptionsToObjects = ImmutableMap.of(
				"Host name", hostname, "user", user, "password", password);
		result = ValidationUtils.checkPredicate(descriptionsToObjects,
				str -> !StringUtils.isBlank(str), desc -> desc
						+ " cannot be empty");
		if (port == 0) {
			result.addError("Post number cannot be equal to 0");
		}
		return result;
	}

	public static EdwardApiConfiguration fromFile(String path)
			throws JsonParseException, JsonMappingException, IOException {
		return fromFile(new File(path));
	}

	public static EdwardApiConfiguration fromFile(File file)
			throws JsonParseException, JsonMappingException, IOException {
		EdwardApiConfiguration configuration = JSON_MAPPER.readValue(file,
				EdwardApiConfiguration.class);
		if (configuration.prefix == null) {
			configuration.prefix = "";
		}
		return configuration;
	}

	@Override
	public String toString() {
		return "EdwardApiConfiguration [hostname=" + hostname + ", port="
				+ port + ", user=" + user + ", password=" + password
				+ ", prefix=" + prefix + "]";
	}

	public String getPrefix() {
		return prefix;
	}

}
