package models.payload;

import models.ConfigValues;
import models.User;

import java.util.List;
import java.util.Objects;

/**
 * @author rishabh
 */
public class Data {

    private String id;
    private String pluginId;
    private String updatedAt;
    private User user;
    private List<ConfigValues> configValues;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

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
        Data data = (Data) o;
        return Objects.equals(id, data.id) &&
                Objects.equals(pluginId, data.pluginId) &&
                Objects.equals(updatedAt, data.updatedAt) &&
                Objects.equals(user, data.user) &&
                Objects.equals(configValues, data.configValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pluginId, updatedAt, user, configValues);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Data{");
        sb.append("id='").append(id).append('\'');
        sb.append(", pluginId='").append(pluginId).append('\'');
        sb.append(", updatedAt='").append(updatedAt).append('\'');
        sb.append(", user=").append(user);
        sb.append(", configValues=").append(configValues);
        sb.append('}');
        return sb.toString();
    }
}
