package daos.impl;

import com.mongodb.WriteResult;
import daos.InstallationDao;
import models.Installation;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import uk.co.panaxiom.playjongo.PlayJongo;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author rishabh
 */
public class InstallationDaoImpl implements InstallationDao {

    private PlayJongo jongo;

    @Inject
    public InstallationDaoImpl(PlayJongo jongo) {
        this.jongo = jongo;
    }

    /**
     * @return The MongoCollection of Installations.
     */
    private MongoCollection installations() {
        return jongo.getCollection("installations");
    }

    private MongoCollection dumps() {
        return jongo.getCollection("dumps");
    }

    @Override
    public List<Installation> getAll(int limit, int skip) {
        MongoCursor cursor = installations().find().skip(skip).limit(limit).as(Installation.class);
        List<Installation> list = new ArrayList<>();
        while (cursor.hasNext()) {
            list.add((Installation) cursor.next());
        }
        return list;
    }

    @Override
    public Installation get(ObjectId id) {
        return installations().findOne(id).as(Installation.class);
    }

    @Override
    public boolean exists(ObjectId id) {
        return get(id) != null;
    }

    @Override
    public WriteResult insert(Installation installation) {
        return installations().insert(installation);
    }

    @Override
    public WriteResult update(Installation installation) {
        return installations().update(installation.getId()).with(installation);
    }

    @Override
    public WriteResult delete(ObjectId id) {
        return installations().remove(id);
    }

    @Override
    public Installation getByUserId(String userId) {
        return installations()
                .findOne("{user.id: #}", userId)
                .as(Installation.class);
    }

    @Override
    public Installation getByUserEmail(String email) {
        return installations()
                .findOne("{user.email: #}", email)
                .as(Installation.class);
    }

    @Override
    public Installation getByUserAndInstallation(String userId, String installationId) {
        return installations()
                .findOne("{$and: [{user.id: #}, {installationId: #}]}", userId, installationId)
                .as(Installation.class);
    }

    @Override
    public Optional<Installation> getByInstallationId(String installationId) {
        return Optional.ofNullable(installations()
                .findOne("{installationId: #}", installationId)
                .as(Installation.class));
    }
}
