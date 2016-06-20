package models;

import java.util.Objects;

/**
 * @author rishabh
 */
public class Metadata {

    private String apiEndpoint;
    private String dc;
    private String loginUrl;

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getDc() {
        return dc;
    }

    public void setDc(String dc) {
        this.dc = dc;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadata metadata = (Metadata) o;
        return Objects.equals(apiEndpoint, metadata.apiEndpoint) &&
                Objects.equals(dc, metadata.dc) &&
                Objects.equals(loginUrl, metadata.loginUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiEndpoint, dc, loginUrl);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Metadata{");
        sb.append("apiEndpoint='").append(apiEndpoint).append('\'');
        sb.append(", dc='").append(dc).append('\'');
        sb.append(", loginUrl='").append(loginUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
