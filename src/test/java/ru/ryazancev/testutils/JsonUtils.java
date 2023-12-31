package ru.ryazancev.testutils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    private static final JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ArrayNode createJsonNodeForObjects(
            List<?> objects, List<String> properties)
            throws JsonProcessingException {
        ArrayNode json = objectMapper.createArrayNode();
        for (Object obj : objects) {
            ObjectNode objNode = createJsonNodeForObject(obj, properties);
            json.add(objectMapper.readTree(objNode.toString()));
        }
        return json;
    }


    public static ObjectNode createJsonNodeForObject(
            Object obj, List<String> properties) {
        ObjectNode objNode = jsonNodeFactory.objectNode();
        for (String prop : properties) {
            try {
                Field field = obj.getClass().getDeclaredField(prop);
                field.setAccessible(true);
                Object value = field.get(obj);
                if (prop.equals("zone")
                        || prop.equals("place")
                        || prop.equals("car")
                        || prop.equals("user")) {
                    ObjectNode nestedNode = jsonNodeFactory.objectNode();
                    List<String> nestedProperties = getNestedProperties(value);

                    for (String nestedProp : nestedProperties) {
                        Field nestedField = value.getClass().getDeclaredField(nestedProp);
                        nestedField.setAccessible(true);
                        Object nestedValue = nestedField.get(value);
                        nestedNode.putPOJO(nestedProp, convertValueToJson(nestedValue));
                    }

                    objNode.set(prop, nestedNode);
                } else {
                    objNode.putPOJO(prop, convertValueToJson(value));
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
        return objNode;
    }

    private static Object convertValueToJson(Object value) {
        if (value instanceof Integer)
            return jsonNodeFactory.numberNode((Integer) value);
        else if (value instanceof Long)
            return jsonNodeFactory.numberNode((Long) value);
        else if (value instanceof Double)
            return jsonNodeFactory.numberNode((Double) value);
        else if (value instanceof LocalDateTime)
            return jsonNodeFactory.textNode(DateUtils.customFormatter
                    .format((LocalDateTime) value));
        else
            return jsonNodeFactory.textNode(String.valueOf(value));
    }

    private static List<String> getNestedProperties(Object value) {
        List<String> nestedProperties = new ArrayList<>();
        Field[] fields = value.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object fieldValue = null;
            try {
                fieldValue = field.get(value);
            } catch (IllegalAccessException ignored) {
            }
            if (fieldValue != null) nestedProperties.add(field.getName());
        }
        return nestedProperties;
    }

    public static String extractJson(JsonNode jsonNode) throws JsonProcessingException {
        StringBuilder result = new StringBuilder("{\"id\":%d," +
                "\"timeFrom\":\"%s\"," +
                "\"timeTo\":\"%s\"," +
                "\"zone\":{\"id\":%d,\"number\":%d}," +
                "\"place\":{\"id\":%d,\"number\":%d}," +
                "\"car\":{\"id\":%d,\"number\":\"%s\"}");

        JsonNode userNode = jsonNode.get("user");
        if (userNode != null) {
            result.append(",\"user\":{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\"}");
            result.append("}");

            return String.format(result.toString(),
                    jsonNode.get("id").asLong(),
                    jsonNode.get("timeFrom").asText(),
                    jsonNode.get("timeTo").asText(),
                    jsonNode.get("zone").get("id").asLong(),
                    jsonNode.get("zone").get("number").asInt(),
                    jsonNode.get("place").get("id").asLong(),
                    jsonNode.get("place").get("number").asInt(),
                    jsonNode.get("car").get("id").asLong(),
                    jsonNode.get("car").get("number").asText(),
                    userNode.get("id").asLong(),
                    userNode.get("name").asText(),
                    userNode.get("email").asText());
        } else {
            result.append("}");
            return String.format(result.toString(),
                    jsonNode.get("id").asLong(),
                    jsonNode.get("timeFrom").asText(),
                    jsonNode.get("timeTo").asText(),
                    jsonNode.get("zone").get("id").asLong(),
                    jsonNode.get("zone").get("number").asInt(),
                    jsonNode.get("place").get("id").asLong(),
                    jsonNode.get("place").get("number").asInt(),
                    jsonNode.get("car").get("id").asLong(),
                    jsonNode.get("car").get("number").asText());
        }
    }

    public static String extractJsonArray(String jsonArray) throws JsonProcessingException {
        JsonNode reservationsArray = objectMapper.readTree(jsonArray);

        StringBuilder result = new StringBuilder("[");

        for (JsonNode reservationNode : reservationsArray) {
            if (result.length() > 1) {
                result.append(",");
            }

            result.append(extractJson(reservationNode));
        }

        result.append("]");

        return result.toString();
    }
}
