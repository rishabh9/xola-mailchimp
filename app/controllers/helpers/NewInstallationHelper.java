package controllers.helpers;

import com.mongodb.WriteResult;
import daos.InstallationDao;
import models.Installation;
import models.payload.Data;
import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;
import play.Logger;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Result;
import utils.Errors;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.INTERNAL_SERVER_ERROR;
import static play.mvc.Results.*;
import static utils.MessageKey.INVALID_JSON;
import static utils.MessageKey.UNEXPECTED_ERROR;

/**
 * @author rishabh
 */
public class NewInstallationHelper {

    private final Logger.ALogger log = Logger.of(NewInstallationHelper.class);

    private final InstallationDao installationDao;
    private final MailchimpKeyVerifier verifier;

    @Inject
    public NewInstallationHelper(InstallationDao installationDao, MailchimpKeyVerifier verifier) {
        this.installationDao = installationDao;
        this.verifier = verifier;
    }

    public Result newInstall(Data data, Messages messages) {
        log.info("Received installation request...");
        if (isInvalid(data)) {
            log.debug("Data object has validation errors");
            return badRequest(Errors.toJson(BAD_REQUEST, messages.at(INVALID_JSON)));
        }
        Installation installation = installationDao.getByUserId(data.getUser().getId());
        if (null != installation) {
            log.debug("Found an existing entry for user {}", data.getUser().getId());
            copyOverData(data, installation);
        } else {
            log.debug("Brand new user {}!", data.getUser().getId());
            installation = createInstallationFromData(data);
        }
        WriteResult result = installationDao.insert(installation);
        if (result.wasAcknowledged()) {
            Installation inst = installationDao.get((ObjectId) result.getUpsertedId());
            CompletableFuture.supplyAsync(() -> verifier.verifyAndCompleteInstallation(inst));
            if (result.isUpdateOfExisting()) {
                log.info("Installation {} updated", result.getUpsertedId().toString());
                return ok(Json.toJson(inst));
            } else {
                log.info("Installation {} created", result.getUpsertedId().toString());
                return created(Json.toJson(inst));
            }
        } else {
            log.error("Error while persisting installation. {}", result.toString());
            return internalServerError(Errors.toJson(INTERNAL_SERVER_ERROR, messages.at(UNEXPECTED_ERROR)));
        }
    }

    private boolean isInvalid(Data data) {
        if (!StringUtils.hasText(data.getId())) {
            log.debug("Missing installation id.");
            return true;
        }
        if (null == data.getUser()) {
            log.debug("Missing user object.");
            return true;
        }
        if (!StringUtils.hasText(data.getUser().getId())) {
            log.debug("Missing user id.");
            return true;
        }
        return false;
    }

    private void copyOverData(Data data, Installation installation) {
        installation.setInstallationId(data.getId());
        if (null != installation.getUser()) {
            installation.getUser().setName(data.getUser().getName());
            installation.getUser().setCompany(data.getUser().getCompany());
            installation.getUser().setEmail(data.getUser().getEmail());
        } else {
            installation.setUser(data.getUser());
        }
        installation.setConfigValues(data.getConfigValues());
    }


    private Installation createInstallationFromData(Data data) {
        Installation installation = new Installation();
        copyOverData(data, installation);
        return installation;
    }
}
