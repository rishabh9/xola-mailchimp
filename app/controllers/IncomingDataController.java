package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import daos.ConfirmationDao;
import models.Confirmation;
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

    private final ConfirmationDao confirmationDao;
    private final WSClient ws;
    private final MessagesApi messagesApi;

    @Inject
    public IncomingDataController(ConfirmationDao confirmationDao, WSClient ws, MessagesApi messagesApi) {
        this.confirmationDao = confirmationDao;
        this.ws = ws;
        this.messagesApi = messagesApi;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> index() {
        log.info("Received request from Xola...");
        JsonNode json = request().body().asJson();
        confirmationDao.dump(json.toString());
        Messages messages = messagesApi.preferred(request());
        String email = json.findPath("customerEmail").textValue();
        String sellerId = json.findPath("seller").findPath("id").textValue();
        log.debug("To add email {} into mailing list of seller {}", email, sellerId);
        if (email == null) {
            log.warn("Incoming data is missing email parameter");
            return CompletableFuture.completedFuture(
                    badRequest(Errors.toJson(BAD_REQUEST, messages.at(MISSING_PARAM_EMAIL))));
        } else {
            log.debug("Making call to Mailchimp to add to mailing list");
            Confirmation confirmation = confirmationDao.getByUserId(sellerId);
            if (null != confirmation && null != confirmation.getList()
                    && StringUtils.hasText(confirmation.getList().getId())) {
                WSRequest request = ws.url(getUrl(confirmation))
                        .setHeader(Http.HeaderNames.AUTHORIZATION, "Bearer " + confirmation.getAccessToken())
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
                return CompletableFuture.completedFuture(internalServerError(
                        Errors.toJson(INTERNAL_SERVER_ERROR, messages.at(MISSING_CONFIG))));
            }
        }
    }

    private String getUrl(Confirmation confirmation) {
        return confirmation.getMetadata().getApiEndpoint()
                + "/3.0/lists/" + confirmation.getList().getId() + "/members";
    }
}
