package pl.joegreen.charles.configuration;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	public boolean isValid() {
		return !(StringUtils.isBlank(hostname) || StringUtils.isBlank(password)
				|| StringUtils.isBlank(user) || port == 0);
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
