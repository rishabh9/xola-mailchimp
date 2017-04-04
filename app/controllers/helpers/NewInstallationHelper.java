package controllers.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.WriteResult;
import daos.InstallationDao;
import models.Installation;
import models.payload.Data;
import org.springframework.util.StringUtils;
import play.Logger;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Result;
import utils.ErrorUtil;
import utils.MessageKey;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        Map<String, String> validationErrors = getValidationErrors(data, messages);
        if (!validationErrors.isEmpty()) {
            log.debug("Data object has validation errors... {}", validationErrors);
            return badRequest(ErrorUtil.toJson(BAD_REQUEST, messages.at(INVALID_JSON), validationErrors));
        }
        Installation installation = installationDao.getByUserId(data.getUser().getId());
        boolean isUpdate = false;
        if (null != installation) {
            log.debug("Found an existing entry for user {}", data.getUser().getId());
            copyOverData(data, installation);
            isUpdate = true;
        } else {
            log.debug("Brand new user {}!", data.getUser().getId());
            installation = createInstallationFromData(data);
        }

        Optional<Result> maybeValidationError = verifier.verifyAndCompleteInstallation(installation, messages);
        if (maybeValidationError.isPresent()) {
            log.error("Error verifying mailchimp api key.");
            return maybeValidationError.get();
        }

        WriteResult result;
        if (isUpdate) {
            result = installationDao.update(installation);
        } else {
            result = installationDao.insert(installation);
        }
        if (result.wasAcknowledged()) {
            log.info("Installation {}: {}", isUpdate ? "updated" : "created", installation.getId());
            return ok(wrap(installation));
        } else {
            log.error("Error while persisting installation. {}", result.toString());
            return internalServerError(ErrorUtil.toJson(INTERNAL_SERVER_ERROR, messages.at(UNEXPECTED_ERROR)));
        }
    }

    private JsonNode wrap(Installation installation) {
        ObjectNode node = Json.newObject();
        node.put("installationId", installation.getInstallationId());
        node.set("user", Json.toJson(installation.getUser()));
        node.set("preferences", Json.toJson(installation.getPreferences()));
        return node;
    }

    private Map<String, String> getValidationErrors(Data data, Messages messages) {
        Map<String, String> errors = new HashMap<>();
        if (!StringUtils.hasText(data.getId())) {
            log.debug("Missing installation id.");
            errors.put("payload.data.id", messages.at(MessageKey.MISSING_INSTALL_ID));
        }
        if (null == data.getUser()) {
            log.debug("Missing user object.");
            errors.put("payload.data.user", messages.at(MessageKey.MISSING_USER_DETAILS));
        }
        if (!StringUtils.hasText(data.getUser().getId())) {
            log.debug("Missing user id.");
            errors.put("payload.data.user.id", messages.at(MessageKey.MISSING_USER_ID));
        }
        if (null == data.getPreferences() || data.getPreferences().isEmpty()) {
            log.debug("Missing preferences");
            errors.put("payload.data.preferences", messages.at(MessageKey.MISSING_PREFERENCES));
        }
        return errors;
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
        installation.setPreferences(data.getPreferences());
    }


    private Installation createInstallationFromData(Data data) {
        Installation installation = new Installation();
        copyOverData(data, installation);
        return installation;
    }
}
