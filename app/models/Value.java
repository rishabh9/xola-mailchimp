package models;

import java.util.Objects;

/**
 * @author rishabh
 */
public final class Value {

    private String id;

    private String label;

    public Value() {
    }

    private Value(Builder builder) {
        setId(builder.id);
        setLabel(builder.label);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return Objects.equals(id, value.id) &&
                Objects.equals(label, value.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label);
    }

    @Override
    public String toString() {
        return "Value{" + "id='" + id + '\'' + ", label='" + label + '\'' + '}';
    }

    public static final class Builder {
        private String id;
        private String label;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Value build() {
            return new Value(this);
        }
    }
}
