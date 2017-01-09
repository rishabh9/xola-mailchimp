package models;

import java.util.Objects;
import java.util.Set;

/**
 * @author rishabh
 */
public class Installation extends BaseModel {

    private String installationId;
    private User user;
    private Set<Preference> preferences;

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
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
        Installation that = (Installation) o;
        return Objects.equals(installationId, that.installationId) &&
                Objects.equals(user, that.user) &&
                Objects.equals(preferences, that.preferences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(installationId, user, preferences);
    }

    @Override
    public String toString() {
        return "Installation{" + "installationId='" + installationId + '\'' + ", user=" + user +
                ", preferences=" + preferences + '}';
    }
}
