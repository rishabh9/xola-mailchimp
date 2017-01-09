package models.payload;

import models.Preference;
import models.User;

import java.util.Objects;
import java.util.Set;

/**
 * @author rishabh
 */
public class Data {

    private String id;
    private String pluginId;
    private String updatedAt;
    private User user;
    private Set<Preference> preferences;

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

    public Set<Preference> getPreferences() {
        return preferences;
    }

    public void setPreferences(Set<Preference> preferences) {
        this.preferences = preferences;
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
                Objects.equals(preferences, data.preferences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pluginId, updatedAt, user, preferences);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Data{");
        sb.append("id='").append(id).append('\'');
        sb.append(", pluginId='").append(pluginId).append('\'');
        sb.append(", updatedAt='").append(updatedAt).append('\'');
        sb.append(", user=").append(user);
        sb.append(", preferences=").append(preferences);
        sb.append('}');
        return sb.toString();
    }
}
