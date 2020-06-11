package onlyid.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Utils {
    static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
}
