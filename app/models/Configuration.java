package models;

import java.util.List;
import java.util.Objects;

/**
 * @author rishabh
 */
public class Configuration {

    private List<ConfigValues> values;

    public List<ConfigValues> getValues() {
        return values;
    }

    public void setValues(List<ConfigValues> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configuration that = (Configuration) o;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Configuration{");
        sb.append("values=").append(values);
        sb.append('}');
        return sb.toString();
    }
}
