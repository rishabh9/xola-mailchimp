package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.helpers.ConfigUpdateHelper;
import controllers.helpers.NewInstallationHelper;
import daos.InstallationDao;
import models.Installation;
import models.payload.Data;
import org.springframework.util.StringUtils;
import play.Configuration;
import play.Logger;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.inject.ConfigurationProvider;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.*;
import utils.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static utils.MessageKey.MISSING_CONFIG;
import static utils.MessageKey.MISSING_PARAM_EMAIL;

/**
 * @author rishabh
 */
@With(AccessLoggingAction.class)
public class IncomingDataController extends Controller {

    private final Logger.ALogger log = Logger.of(IncomingDataController.class);

    private final InstallationDao installationDao;
    private final WSClient ws;
    private final MessagesApi messagesApi;
    private final NewInstallationHelper installationHelper;
    private final ConfigUpdateHelper updateHelper;
    private final Configuration config;
    private final InstallationUtility utility;

    @Inject
    public IncomingDataController(InstallationDao installationDao, WSClient ws, MessagesApi messagesApi,
                                  NewInstallationHelper installationHelper, ConfigUpdateHelper updateHelper,
                                  ConfigurationProvider configProvider, InstallationUtility utility) {
        this.installationDao = installationDao;
        this.ws = ws;
        this.messagesApi = messagesApi;
        this.installationHelper = installationHelper;
        this.updateHelper = updateHelper;
        this.config = configProvider.get();
        this.utility = utility;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> index() {
        log.info("Received request from Xola...");
        JsonNode json = request().body().asJson();
        Messages messages = messagesApi.preferred(request());
        String event = json.findPath("eventName").textValue();
        if (StringUtils.hasText(event)) {
            log.debug("Received event {}", event);
            switch (event) {
                case Event.ORDER_CREATE:
                case Event.ORDER_UPDATE:
                    log.debug("Order event received");
                    return executeOrderEvent(json, messages);
                case Event.PLUGIN_CONFIG_UPDATE:
                case Event.PLUGIN_INSTALL:
                    log.debug("Installation event received");
                    return complete(executeInstallationEvents(event, json, messages));
                case Event.PLUGIN_UNINSTALL:
                    log.debug("Uninstall event received");
                    return complete(ok());
                default:
                    log.warn("Ignoring event {}.", event);
                    return complete(badRequest(ErrorUtil.toJson(BAD_REQUEST, messages.at(MessageKey.NOT_SUBSCRIBED))));
            }
        } else {
            log.error("Missing 'eventName' tag.");
            return complete(badRequest(ErrorUtil.toJson(BAD_REQUEST, messages.at(MessageKey.INVALID_JSON))));
        }
    }

    private CompletionStage<Result> executeOrderEvent(JsonNode json, Messages messages) {
        String email = json.findPath("customerEmail").textValue();
        String sellerId = json.findPath("seller").findPath("id").textValue();
        log.debug("To add email {} into mailing list of seller {}", email, sellerId);
        if (email == null) {
            log.warn("Incoming data is missing email parameter");
            return complete(badRequest(ErrorUtil.toJson(BAD_REQUEST, messages.at(MISSING_PARAM_EMAIL))));
        } else {
            log.debug("Making call to Mailchimp to add to mailing list");
            Installation installation = installationDao.getByUserId(sellerId);
            Optional<String> apiKey = utility.getApiKey(installation);
            Optional<String> configuredListId = utility.getConfiguredListId(installation);
            if (null != installation && configuredListId.isPresent() && apiKey.isPresent()) {
                WSRequest request = ws.url(getUrl(installation, configuredListId.get()))
                        .setAuth("username", apiKey.get())
                        .setContentType(Http.MimeTypes.JSON);
                ObjectNode data = Json.newObject();
                data.put("email_address", email);
                data.put("status", "subscribed");
                return request.post(data).thenApply(wsResponse -> {
                    JsonNode jsonResponse = wsResponse.asJson();
                    log.debug("Response from MailChimp {}", jsonResponse.asText());
                    return ok(jsonResponse);
                });
            } else {
                log.error("Did not find configuration for user {}. Couldn't connect to MailChimp.", sellerId);
                return complete(internalServerError(
                        ErrorUtil.toJson(INTERNAL_SERVER_ERROR, messages.at(MISSING_CONFIG))));
            }
        }
    }

    private String getUrl(Installation installation, String configuredListId) {
        return String.format(
                config.getString(Constants.MAILCHIMP_ADD_EMAIL_URL),
                utility.getDataCentre(installation).get(), configuredListId);
    }

    private Result executeInstallationEvents(String event, JsonNode json, Messages messages) {
        Data data;
        try {
            data = Json.fromJson(json.findPath("data"), Data.class);
        } catch (Exception e) {
            log.error("Missing or invalid data object.", e);
            Map<String, String> errorMessages = new HashMap<>();
            errorMessages.put("payload.data", messages.at(MessageKey.MISSING_PAYLOAD_DATA));
            return badRequest(ErrorUtil.toJson(BAD_REQUEST, messages.at(MessageKey.INVALID_JSON), errorMessages));
        }
        if (event.equals(Event.PLUGIN_INSTALL)) {
            return installationHelper.newInstall(data, messages);
        } else {
            return updateHelper.updateConfiguration(data, messages);
        }
    }

    private CompletionStage<Result> complete(Result result) {
        return CompletableFuture.completedFuture(result);
    }
}
