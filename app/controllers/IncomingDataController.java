package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.helpers.NewInstallationHelper;
import daos.InstallationDao;
import models.Installation;
import org.springframework.util.StringUtils;
import play.Logger;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.Errors;
import utils.MessageKey;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static utils.MessageKey.MISSING_CONFIG;
import static utils.MessageKey.MISSING_PARAM_EMAIL;

/**
 * @author rishabh
 */
public class IncomingDataController extends Controller {

    private final Logger.ALogger log = Logger.of(IncomingDataController.class);

    private final InstallationDao installationDao;
    private final WSClient ws;
    private final MessagesApi messagesApi;
    private final NewInstallationHelper installationHelper;

    @Inject
    public IncomingDataController(InstallationDao installationDao, WSClient ws, MessagesApi messagesApi,
                                  NewInstallationHelper installationHelper) {
        this.installationDao = installationDao;
        this.ws = ws;
        this.messagesApi = messagesApi;
        this.installationHelper = installationHelper;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> index() {
        log.info("Received request from Xola...");
        JsonNode json = request().body().asJson();
        installationDao.dump(json.toString());
        Messages messages = messagesApi.preferred(request());
        String event = json.findPath("eventName").textValue();
        if (StringUtils.hasText(event)) {
            log.debug("Received event {}", event);
            switch (event) {
                case "order.create":
                case "order.update":
                    return executeOrderEvent(json, messages);
                case "config.update":
                case "new.install":
                    return complete(installationHelper.initiateInstall(json.findPath("data"), messages));
                default:
                    log.warn("Ignoring event {}.");
                    return complete(badRequest(Errors.toJson(BAD_REQUEST, messages.at(MessageKey.NOT_SUBSCRIBED))));
            }
        } else {
            log.error("Missing 'eventName' tag.");
            return complete(badRequest(Errors.toJson(BAD_REQUEST, messages.at(MessageKey.INVALID_JSON))));
        }
    }

    private CompletionStage<Result> executeOrderEvent(JsonNode json, Messages messages) {
        String email = json.findPath("customerEmail").textValue();
        String sellerId = json.findPath("seller").findPath("id").textValue();
        log.debug("To add email {} into mailing list of seller {}", email, sellerId);
        if (email == null) {
            log.warn("Incoming data is missing email parameter");
            return complete(badRequest(Errors.toJson(BAD_REQUEST, messages.at(MISSING_PARAM_EMAIL))));
        } else {
            log.debug("Making call to Mailchimp to add to mailing list");
            Installation installation = installationDao.getByUserId(sellerId);
            if (null != installation && null != installation.getList()
                    && StringUtils.hasText(installation.getList().getId())) {
                WSRequest request = ws.url(getUrl(installation))
                        .setHeader(Http.HeaderNames.AUTHORIZATION, "Bearer " + installation.getAccessToken())
                        .setContentType(Http.MimeTypes.JSON);
                ObjectNode data = Json.newObject();
                data.put("email_address", email);
                data.put("status", "subscribed");
                return request.post(data).thenApply(wsResponse -> {
                    JsonNode jsonResponse = wsResponse.asJson();
                    log.debug("Response from Mailchimp {}", jsonResponse.asText());
                    return ok(jsonResponse);
                });
            } else {
                log.error("Did not find configuration for user {}", email);
                return complete(internalServerError(
                        Errors.toJson(INTERNAL_SERVER_ERROR, messages.at(MISSING_CONFIG))));
            }
        }
    }

    private String getUrl(Installation installation) {
        return installation.getMetadata().getApiEndpoint()
                + "/3.0/lists/" + installation.getList().getId() + "/members";
    }

    private CompletionStage<Result> complete(Result result) {
        return CompletableFuture.completedFuture(result);
    }
}
