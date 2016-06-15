package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * Base class for Model objects. Child objects should implement toString(),
 * equals() and hashCode().
 *
 * @author rishabh
 */
abstract class BaseModel<T, PK extends ObjectId> implements Serializable {

    /**
     * The Mongo Object ID for the given Document.
     */
    @JsonProperty("_id")
    public PK id;

    /**
     * @return The Mongo Object ID for given Document.
     */
    public PK getId() {
        return id;
    }

    /**
     * Set the Mongo Object ID for the given Document.
     *
     * @param id The Mongo Object ID to set for the given Document.
     */
    public void setId(final PK id) {
        this.id = id;
    }

    /**
     * Returns a multi-line String with key=value pairs.
     *
     * @return a String representation of this class.
     */
    public abstract String toString();

    /**
     * Compares object equality. When using Hibernate, the primary key should
     * not be a part of this comparison.
     *
     * @param o object to compare to
     * @return true/false based on equality tests
     */
    public abstract boolean equals(Object o);

    /**
     * When you override equals, you should override hashCode.
     *
     * @return hashCode
     */
    public abstract int hashCode();
}
