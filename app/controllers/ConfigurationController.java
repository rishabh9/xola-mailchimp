package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.WriteResult;
import daos.ConfirmationDao;
import models.Configuration;
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
import utils.MessageKey;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author rishabh
 */
public class ConfigurationController extends Controller {

    private final transient Logger.ALogger log = Logger.of(ConfigurationController.class);

    private final MessagesApi messagesApi;
    private final ConfirmationDao confirmationDao;

    @Inject
    public ConfigurationController(MessagesApi messagesApi, ConfirmationDao confirmationDao) {
        super();
        this.messagesApi = messagesApi;
        this.confirmationDao = confirmationDao;
    }

    /**
     * Update the plugin configuration provided by Xola for the given installation id.
     *
     * @param installationId The installation for which the configuration is provided.
     * @return The installation's configuration
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateConfiguration(String installationId) {
        log.info("Received request to update the plugin configuration for installation {}", installationId);
        final Messages messages = messagesApi.preferred(request());
        if (!StringUtils.hasText(installationId)) {
            log.debug("Invalid installation id provided: {}", installationId);
            return badRequest(Errors.toJson(BAD_REQUEST, messages.at(MessageKey.MISSING_INSTLL_ID)));
        }
        Optional<Confirmation> confirmation = confirmationDao.getByInstallationId(installationId);
        if (!confirmation.isPresent()) {
            log.debug("Installation with the given installation id was not found: {}", installationId);
            return notFound(Errors.toJson(NOT_FOUND, messages.at(MessageKey.NOT_FOUND)));
        }
        JsonNode json = request().body().asJson();
        Configuration configuration = Json.fromJson(json, Configuration.class);
        if (null == configuration || null == configuration.getValues() || configuration.getValues().isEmpty()) {
            log.debug("Invalid request received: {}", json);
            badRequest(Errors.toJson(BAD_REQUEST, messages.at(MessageKey.INVALID_JSON)));
        }
        Confirmation confirm = confirmation.get();
        confirm.setConfiguration(configuration);
        WriteResult result = confirmationDao.insert(confirm);
        if (result.wasAcknowledged()) {
            log.debug("Configuration saved successfully for installation {}", installationId);
            return ok(Json.toJson(confirmationDao.get(confirm.getId()).getConfiguration()));
        } else {
            log.debug("Error saving configuration for installation {}", installationId);
            return internalServerError(Errors.toJson(INTERNAL_SERVER_ERROR, MessageKey.UNEXPECTED_ERROR));
        }
    }

    /**
     * Get the plugin configuration for the given installation id.
     *
     * @param installationId The installation for which the configuration is required.
     * @return The installation's configuration
     */
    @BodyParser.Of(BodyParser.Empty.class)
    public Result getConfiguration(String installationId) {
        log.info("Received request to retrieve the plugin configuration for installation {}", installationId);
        final Messages messages = messagesApi.preferred(request());
        if (!StringUtils.hasText(installationId)) {
            log.debug("Invalid installation id provided: {}", installationId);
            return badRequest(Errors.toJson(BAD_REQUEST, messages.at(MessageKey.MISSING_INSTLL_ID)));
        }
        Optional<Confirmation> confirmation = confirmationDao.getByInstallationId(installationId);
        if (!confirmation.isPresent()) {
            log.debug("Installation with the given installation id was not found: {}", installationId);
            return notFound(Errors.toJson(NOT_FOUND, messages.at(MessageKey.NOT_FOUND)));
        }
        log.debug("Retrieving configuration for installation id {}", installationId);
        return ok(Json.toJson(confirmation.get().getConfiguration()));
    }

}
