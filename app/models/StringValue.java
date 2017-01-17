package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rishabh
 */
public final class StringValue extends Value<String> {

    @JsonCreator
    public StringValue(@JsonProperty("id") String id, @JsonProperty("label") String label) {
        super(TYPE_STRING);
        setId(id);
        setLabel(label);
    }
}
