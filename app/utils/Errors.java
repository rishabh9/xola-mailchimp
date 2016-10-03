package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import play.libs.Json;

import java.util.List;

/**
 * @author rishabh
 */
public class Errors {

    private static final String ERRORS = "errors";
    private static final String CODE = "code";
    private static final String MESSAGE = "message";

    private static JsonNode toJson(String code, String message) {
        return Json.newObject()
                .set(ERRORS, Json.newArray().add(
                        Json.newObject().put(CODE, code).put(MESSAGE, message)));
    }

    public static JsonNode toJson(int code, String message) {
        return toJson(Integer.toString(code), message);
    }

    public static JsonNode toJson(String code, List<String> messages) {
        ArrayNode errArray = Json.newArray();
        messages.forEach(msg -> errArray.add(
                Json.newObject().put(CODE, code).put(MESSAGE, msg)));
        return Json.newObject().set(ERRORS, errArray);
    }

    public static JsonNode toJson(int code, List<String> messages) {
        return toJson(Integer.toString(code), messages);
    }
}

