package pl.joegreen.edward.charles.configuration.model;

import java.util.Map;

public class Population {

	private Map<Object, Object> mapRepresentation;

	public Map<Object, Object> getMapRepresentation() {
		return mapRepresentation;
	}

	public Population(Map<Object, Object> map) {
		this.mapRepresentation = map;
	}

	public void put(Object key, Object value) {
		mapRepresentation.put(key, value);
	}

	public Object get(Object key) {
		return mapRepresentation.get(key);
	}

}
