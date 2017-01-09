package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rishabh
 */
public class IntegerValue extends BaseValue<Long> {

    @JsonCreator
    public IntegerValue(@JsonProperty("id") String id, @JsonProperty("label") Long label) {
        super();
        setId(id);
        setLabel(label);
    }
}
