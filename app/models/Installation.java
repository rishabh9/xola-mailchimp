package models;

import java.util.Objects;

/**
 * @author rishabh
 */
public class Installation extends BaseModel {

    private String installationId;
    private User user;
    private String oauthCode;
    private String accessToken;
    private Metadata metadata;
    private MailingList list;

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

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public MailingList getList() {
        return list;
    }

    public void setList(MailingList list) {
        this.list = list;
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
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(installationId, user, oauthCode, accessToken, metadata, list);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Installation{");
        sb.append("installationId='").append(installationId).append('\'');
        sb.append(", user=").append(user);
        sb.append(", oauthCode='").append(oauthCode).append('\'');
        sb.append(", accessToken='").append(accessToken).append('\'');
        sb.append(", metadata=").append(metadata);
        sb.append(", list=").append(list);
        sb.append('}');
        return sb.toString();
    }
}
