package com.zz.platform.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JsonUtil() {
    }

    public static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("对象序列化失败", ex);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("JSON反序列化失败", ex);
        }
    }

    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }
}
