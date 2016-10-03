package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.WriteResult;
import daos.ConfirmationDao;
import models.Confirmation;
import org.springframework.util.StringUtils;
import play.Logger;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Errors;

import javax.inject.Inject;

import static utils.MessageKey.INVALID_JSON;
import static utils.MessageKey.UNEXPECTED_ERROR;

/**
 * @author rishabh
 */
public class InstallationController extends Controller {

    private final Logger.ALogger log = Logger.of(InstallationController.class);

    private final ConfirmationDao confirmationDao;
    private final MessagesApi messagesApi;

    @Inject
    public InstallationController(ConfirmationDao confirmationDao, MessagesApi messagesApi) {
        this.confirmationDao = confirmationDao;
        this.messagesApi = messagesApi;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result initiateInstall() {
        log.info("Received installation request...");
        Messages messages = messagesApi.preferred(request());
        JsonNode json = request().body().asJson();
        Confirmation confirmation;
        try {
            confirmation = Json.fromJson(json, Confirmation.class);
            log.debug("Received confirmation: {}", confirmation.toString());
        } catch (Exception e) {
            log.error("Error in parsing the confirmation data!", e);
            log.error("Received the following data: {}", json.toString());
            return badRequest(Errors.toJson(BAD_REQUEST, messages.at(INVALID_JSON)));
        }
        if (isInvalid(confirmation)) {
            log.debug("Received invalid json in request - {}", json.toString());
            return badRequest(Errors.toJson(BAD_REQUEST, messages.at(INVALID_JSON)));
        }
        String userId = confirmation.getUser().getId();
        Confirmation existingConfirmation = confirmationDao.getByUserId(userId);
        if (null != existingConfirmation) {
            log.debug("Found an existing entry for user {}", userId);
            copyOverData(confirmation, existingConfirmation);
        } else {
            log.debug("Brand new user {}!", userId);
            existingConfirmation = confirmation;
        }
        WriteResult result = confirmationDao.insert(existingConfirmation);
        if (result.wasAcknowledged()) {
            final String upsertedId;
            if (result.isUpdateOfExisting()) {
                upsertedId = existingConfirmation.getId().toString();
                log.info("Confirmation {} updated", upsertedId);
                return ok(confirmationJson(upsertedId));
            } else {
                upsertedId = result.getUpsertedId().toString();
                log.info("Confirmation {} created", upsertedId);
                return created(confirmationJson(upsertedId));
            }
        } else {
            log.error("Error while persisting confirmation. {}", result.toString());
            return internalServerError(Errors.toJson(INTERNAL_SERVER_ERROR, messages.at(UNEXPECTED_ERROR)));
        }
    }

    private JsonNode confirmationJson(String upsertedId) {
        ObjectNode json = Json.newObject();
        json.put("id", upsertedId);
        return json;
    }

    private boolean isInvalid(Confirmation newData) {
        if (!StringUtils.hasText(newData.getInstallationId()))
            return true;
        if (null == newData.getUser())
            return true;
        if (!StringUtils.hasText(newData.getUser().getId()))
            return true;
        return false;
    }

    private void copyOverData(Confirmation source, Confirmation destination) {
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
