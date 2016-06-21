package models;

import java.util.Objects;

/**
 * @author rishabh
 */
public class Confirmation extends BaseModel {

    private String pluginId;
    private Seller seller;
    private String oauthCode;
    private String accessToken;
    private Metadata metadata;
    private MailingList list;

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public Seller getSeller() {
        return seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
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
        Confirmation that = (Confirmation) o;
        return Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(seller, that.seller) &&
                Objects.equals(oauthCode, that.oauthCode) &&
                Objects.equals(accessToken, that.accessToken) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, seller, oauthCode, accessToken, metadata, list);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Confirmation{");
        sb.append("pluginId='").append(pluginId).append('\'');
        sb.append(", seller=").append(seller);
        sb.append(", oauthCode='").append(oauthCode).append('\'');
        sb.append(", accessToken='").append(accessToken).append('\'');
        sb.append(", metadata=").append(metadata);
        sb.append(", list=").append(list);
        sb.append('}');
        return sb.toString();
    }
}
