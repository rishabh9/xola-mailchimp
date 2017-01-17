package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rishabh
 */
public class FloatValue extends Value<Double> {

    @JsonCreator
    public FloatValue(@JsonProperty("id") String id, @JsonProperty("label") Double label) {
        super(TYPE_DECIMAL);
        setId(id);
        setLabel(label);
    }
}
