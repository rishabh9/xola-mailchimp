package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rishabh
 */
public class BooleanValue extends Value<Boolean> {

    @JsonCreator
    public BooleanValue(@JsonProperty("id") String id, @JsonProperty("label") Boolean label) {
        super();
        setId(id);
        setLabel(label);
    }
}
