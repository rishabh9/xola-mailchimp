package utils;

import com.fasterxml.jackson.databind.JsonNode;
import models.Error;
import models.Errors;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author rishabh
 */
public class ErrorUtil {

    private static Error getErrorObject(int code, String message) {
        return Error.newBuilder().code(Integer.toString(code)).message(message).build();
    }

    private static List<Errors> getErrorList(Map<String, String> messages) {
        List<Errors> errors = new ArrayList<>(messages.size());
        messages.forEach((key, value) -> errors.add(Errors.newBuilder().key(key).message(value).build()));
        return errors;
    }

    public static JsonNode toJson(int code, String message) {
        return Json.toJson(getErrorObject(code, message));
    }

    public static JsonNode toJson(int code, String commonMessage, Map<String, String> messages) {
        Error error = getErrorObject(code, commonMessage);
        if (null != messages && !messages.isEmpty()) {
            error.setErrors(getErrorList(messages));
        }
        return Json.toJson(error);
    }

}

