package pl.joegreen.charles.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class JsonUtil {

    private final static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

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
}
