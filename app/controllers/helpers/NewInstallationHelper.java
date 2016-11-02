package controllers.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.WriteResult;
import daos.InstallationDao;
import models.Installation;
import org.springframework.util.StringUtils;
import play.Logger;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import utils.Errors;

import javax.inject.Inject;

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

    @Inject
    public NewInstallationHelper(InstallationDao installationDao) {
        this.installationDao = installationDao;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result initiateInstall(JsonNode json, Messages messages) {
        log.info("Received installation request...");
        Installation installation;
        try {
            installation = Json.fromJson(json, Installation.class);
            log.debug("Received installation: {}", installation.toString());
        } catch (Exception e) {
            log.error("Error in parsing the installation data!", e);
            log.error("Received the following data: {}", json.toString());
            return badRequest(Errors.toJson(BAD_REQUEST, messages.at(INVALID_JSON)));
        }
        if (isInvalid(installation)) {
            log.debug("Received invalid json in request - {}", json.toString());
            return badRequest(Errors.toJson(BAD_REQUEST, messages.at(INVALID_JSON)));
        }
        String userId = installation.getUser().getId();
        Installation existingInstallation = installationDao.getByUserId(userId);
        if (null != existingInstallation) {
            log.debug("Found an existing entry for user {}", userId);
            copyOverData(installation, existingInstallation);
        } else {
            log.debug("Brand new user {}!", userId);
            existingInstallation = installation;
        }
        WriteResult result = installationDao.insert(existingInstallation);
        if (result.wasAcknowledged()) {
            final String upsertedId;
            if (result.isUpdateOfExisting()) {
                upsertedId = existingInstallation.getId().toString();
                log.info("Installation {} updated", upsertedId);
                return ok(confirmationJson(upsertedId));
            } else {
                upsertedId = result.getUpsertedId().toString();
                log.info("Installation {} created", upsertedId);
                return created(confirmationJson(upsertedId));
            }
        } else {
            log.error("Error while persisting installation. {}", result.toString());
            return internalServerError(Errors.toJson(INTERNAL_SERVER_ERROR, messages.at(UNEXPECTED_ERROR)));
        }
    }

    private JsonNode confirmationJson(String upsertedId) {
        ObjectNode json = Json.newObject();
        json.put("id", upsertedId);
        return json;
    }

    private boolean isInvalid(Installation newData) {
        if (!StringUtils.hasText(newData.getInstallationId()))
            return true;
        if (null == newData.getUser())
            return true;
        if (!StringUtils.hasText(newData.getUser().getId()))
            return true;
        return false;
    }

    private void copyOverData(Installation source, Installation destination) {
        destination.setInstallationId(source.getInstallationId());
        if (null != destination.getUser()) {
            destination.getUser().setName(source.getUser().getName());
            destination.getUser().setCompany(source.getUser().getCompany());
            destination.getUser().setEmail(source.getUser().getEmail());
        } else {
            destination.setUser(source.getUser());
        }
    }
}
