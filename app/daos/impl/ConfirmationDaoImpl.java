package daos.impl;

import com.mongodb.WriteResult;
import daos.ConfirmationDao;
import models.Confirmation;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import uk.co.panaxiom.playjongo.PlayJongo;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rishabh
 */
public class ConfirmationDaoImpl implements ConfirmationDao {

    private PlayJongo jongo;

    @Inject
    public ConfirmationDaoImpl(PlayJongo jongo) {
        this.jongo = jongo;
    }

    /**
     * @return The MongoCollection of Installations.
     */
    private MongoCollection confirmations() {
        return jongo.getCollection("confirmations");
    }

    private MongoCollection dumps() {
        return jongo.getCollection("dumps");
    }

    @Override
    public List<Confirmation> getAll(int limit, int skip) {
        MongoCursor cursor = confirmations().find().skip(skip).limit(limit).as(Confirmation.class);
        List<Confirmation> list = new ArrayList<>();
        while (cursor.hasNext()) {
            list.add((Confirmation) cursor.next());
        }
        return list;
    }

    @Override
    public Confirmation get(ObjectId id) {
        return confirmations().findOne(id).as(Confirmation.class);
    }

    @Override
    public boolean exists(ObjectId id) {
        return get(id) != null;
    }

    @Override
    public WriteResult insert(Confirmation data) {
        return confirmations().save(data);
    }

    @Override
    public WriteResult delete(Confirmation data) {
        return confirmations().remove(data.getId());
    }

    @Override
    public WriteResult delete(ObjectId id) {
        return confirmations().remove(id);
    }

    @Override
    public Confirmation getByUserId(String userId) {
        return confirmations()
                .findOne("{user.id: #}", userId)
                .as(Confirmation.class);
    }

    @Override
    public Confirmation getByUserEmail(String email) {
        return confirmations()
                .findOne("{user.email: #}", email)
                .as(Confirmation.class);
    }

    @Override
    public void dump(String json) {
        dumps().insert(json);
    }
}
