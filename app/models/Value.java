package models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

/**
 * @author rishabh
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = StringValue.class, name = "string"),
        @JsonSubTypes.Type(value = IntegerValue.class, name = "number"),
        @JsonSubTypes.Type(value = FloatValue.class, name = "decimal"),
        @JsonSubTypes.Type(value = BooleanValue.class, name = "boolean"),
})
public abstract class Value<T> {
    private String type;
    private String id;
    private T label;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public T getLabel() {
        return label;
    }

    public void setLabel(T label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value<?> value = (Value<?>) o;
        return
                Objects.equals(type, value.type) &&
                        Objects.equals(id, value.id) &&
                        Objects.equals(label, value.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, label);
    }

    @Override
    public String toString() {
        return "Value{" + "type='" + type + '\'' + ", id='" + id + '\'' + ", label=" + label + '}';
    }
}
