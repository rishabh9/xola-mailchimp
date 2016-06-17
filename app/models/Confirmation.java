package models;

import java.util.Objects;

/**
 * @author rishabh
 */
public class Confirmation extends BaseModel {

    private String uid;
    private String ts;

    public Confirmation(String uid, String ts) {
        this.uid = uid;
        this.ts = ts;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Confirmation that = (Confirmation) o;
        return Objects.equals(uid, that.uid) &&
                Objects.equals(ts, that.ts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, ts);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Confirmation-{");
        sb.append("uid='").append(uid).append('\'');
        sb.append(", ts='").append(ts).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
