package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.WriteResult;
import controllers.helpers.MailChimpAuthorizeCall;
import daos.ConfirmationDao;
import models.Confirmation;
import play.Logger;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

import static utils.MessageKey.UNEXPECTED_ERROR;

/**
 * @author rishabh
 */
public class SetupController extends Controller {

    private final Logger.ALogger log = Logger.of(SetupController.class);

    private final ConfirmationDao confirmationDao;
    private final MessagesApi messagesApi;
    private final MailChimpAuthorizeCall authorizeCall;

    @Inject
    public SetupController(final ConfirmationDao confirmationDao, final MessagesApi messagesApi,
                           final MailChimpAuthorizeCall authorizeCall) {
        super();
        this.confirmationDao = confirmationDao;
        this.messagesApi = messagesApi;
        this.authorizeCall = authorizeCall;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result setup() {
        JsonNode json = request().body().asJson();
        Confirmation data;
        try {
            data = Json.fromJson(json, Confirmation.class);
            log.debug("Received confirmation: {}", data.toString());
        } catch (Exception e) {
            log.error("Error in parsing the confirmation data!", e);
            log.error("Received the following data: {}", json.toString());
            return badRequest();
        }

        WriteResult result = confirmationDao.insert(data);
        if (result.wasAcknowledged()) {
            final String upsertedId = result.getUpsertedId().toString();
            log.info("Confirmation {} created", upsertedId);
            authorizeCall.setId(upsertedId);
            return redirect(authorizeCall);
        } else {
            log.error("Error while persisting confirmation. {}", result.toString());
            return internalServerError(getMsg().at(UNEXPECTED_ERROR));
        }
    }

    private Messages getMsg() {
        return messagesApi.preferred(request());
    }
}
