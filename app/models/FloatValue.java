package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rishabh
 */
public class FloatValue extends BaseValue<Double> {

    @JsonCreator
    public FloatValue(@JsonProperty("id") String id, @JsonProperty("label") Double label) {
        super();
        setId(id);
        setLabel(label);
    }
}
