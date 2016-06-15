package daos;

import com.mongodb.WriteResult;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * @author rishabh
 */
interface GenericDao<T, PK extends ObjectId> {

    /**
     * Generic method used to get all documents of a particular users.
     *
     * @return List of documents.
     */
    List<T> getAll(int limit, int skip);

    /**
     * Generic method to get a document based on class and identifier.
     *
     * @param id the identifier (primary key) of the document to get.
     * @return a populated document object.
     */
    T get(PK id);

    /**
     * Checks for existence of a document of type T using the id arg.
     *
     * @param id the id of the entity.
     * @return - true if it exists, false if it doesn't.
     */
    boolean exists(PK id);

    /**
     * Generic method to insert a document.
     *
     * @param object the document to save.
     * @return the result of the operation as a {@link WriteResult} object.
     */
    WriteResult insert(T object);

    /**
     * Generic method to delete a document.
     *
     * @param object the document to delete.
     * @return the result of the operation as a {@link WriteResult} object.
     */
    WriteResult delete(T object);

    /**
     * Generic method to delete a document.
     *
     * @param id the identifier (primary key) of the document to remove.
     * @return the result of the operation as a {@link WriteResult} object.
     */
    WriteResult delete(PK id);
}
