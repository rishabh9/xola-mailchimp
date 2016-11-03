package models;

import java.util.List;
import java.util.Objects;

/**
 * @author rishabh
 */
public class Configuration {

    private List<ConfigValues> configValues;

    public List<ConfigValues> getConfigValues() {
        return configValues;
    }

    public void setConfigValues(List<ConfigValues> configValues) {
        this.configValues = configValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configuration that = (Configuration) o;
        return Objects.equals(configValues, that.configValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configValues);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Configuration{");
        sb.append("configValues=").append(configValues);
        sb.append('}');
        return sb.toString();
    }
}
