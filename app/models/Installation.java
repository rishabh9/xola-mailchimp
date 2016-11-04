package models;

import java.util.List;
import java.util.Objects;

/**
 * @author rishabh
 */
public class Installation extends BaseModel {

    private String installationId;
    private User user;
    private String oauthCode;
    private String accessToken;
    private MailingList list;
    private List<ConfigValues> configValues;

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

    public String getOauthCode() {
        return oauthCode;
    }

    public void setOauthCode(String oauthCode) {
        this.oauthCode = oauthCode;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public MailingList getList() {
        return list;
    }

    public void setList(MailingList list) {
        this.list = list;
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
        Installation that = (Installation) o;
        return Objects.equals(installationId, that.installationId) &&
                Objects.equals(user, that.user) &&
                Objects.equals(oauthCode, that.oauthCode) &&
                Objects.equals(accessToken, that.accessToken) &&
                Objects.equals(list, that.list) &&
                Objects.equals(configValues, that.configValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(installationId, user, oauthCode, accessToken, list, configValues);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Installation{");
        sb.append("installationId='").append(installationId).append('\'');
        sb.append(", user=").append(user);
        sb.append(", oauthCode='").append(oauthCode).append('\'');
        sb.append(", accessToken='").append(accessToken).append('\'');
        sb.append(", list=").append(list);
        sb.append(", configuration=").append(configValues);
        sb.append('}');
        return sb.toString();
    }
}
