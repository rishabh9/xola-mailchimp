package models;

import java.util.List;
import java.util.Objects;

/**
 * @author rishabh
 */
public class Error {

    private String code;
    private String message;
    private List<Errors> errors;

    private Error(Builder builder) {
        setCode(builder.code);
        setMessage(builder.message);
        setErrors(builder.errors);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Errors> getErrors() {
        return errors;
    }

    public void setErrors(List<Errors> errors) {
        this.errors = errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Error error = (Error) o;
        return Objects.equals(code, error.code) &&
                Objects.equals(message, error.message) &&
                Objects.equals(errors, error.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, errors);
    }

    @Override
    public String toString() {
        return "Error{" + "code='" + code + '\'' + ", message='" + message + '\'' + ", errors=" + errors + '}';
    }

    public static final class Builder {
        private String code;
        private String message;
        private List<Errors> errors;

        private Builder() {
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder errors(List<Errors> errors) {
            this.errors = errors;
            return this;
        }

        public Error build() {
            return new Error(this);
        }
    }
}


