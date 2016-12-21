package daos;

import com.google.inject.ImplementedBy;
import daos.impl.InstallationDaoImpl;
import models.Installation;
import org.bson.types.ObjectId;

import java.util.Optional;

/**
 * @author rishabh
 */
@ImplementedBy(InstallationDaoImpl.class)
public interface InstallationDao extends GenericDao<Installation, ObjectId> {

    Installation getByUserId(String userId);

    Installation getByUserEmail(String email);

    Installation getByUserAndInstallation(String userId, String installationId);

    Optional<Installation> getByInstallationId(String installationId);

    void dump(String json);
}
