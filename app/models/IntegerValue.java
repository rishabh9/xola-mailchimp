package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rishabh
 */
public class IntegerValue extends Value<Long> {

    @JsonCreator
    public IntegerValue(@JsonProperty("id") String id, @JsonProperty("label") Long label) {
        super(TYPE_NUMBER);
        setId(id);
        setLabel(label);
    }
}
