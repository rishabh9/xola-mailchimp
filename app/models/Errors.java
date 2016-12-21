package models;

import java.util.Objects;

/**
 * @author rishabh
 */
public class Errors {

    private String key;
    private String message;

    private Errors(Builder builder) {
        setKey(builder.key);
        setMessage(builder.message);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Errors errors = (Errors) o;
        return Objects.equals(key, errors.key) &&
                Objects.equals(message, errors.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, message);
    }

    @Override
    public String toString() {
        return "Errors{" + "key='" + key + '\'' + ", message='" + message + '\'' + '}';
    }

    public static final class Builder {
        private String key;
        private String message;

        private Builder() {
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Errors build() {
            return new Errors(this);
        }
    }
}
