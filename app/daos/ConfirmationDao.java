package daos;

import com.google.inject.ImplementedBy;
import daos.impl.ConfirmationDaoImpl;
import models.Confirmation;
import org.bson.types.ObjectId;

/**
 * @author rishabh
 */
@ImplementedBy(ConfirmationDaoImpl.class)
public interface ConfirmationDao extends GenericDao<Confirmation, ObjectId> {

    Confirmation getByUserId(String userId);
    Confirmation getByUserEmail(String email);

    Confirmation getByUserAndInstallation(String userId, String installationId);
    void dump(String json);
}
