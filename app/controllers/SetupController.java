package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.WriteResult;
import controllers.helpers.MailChimpAuthorizeCall;
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

import javax.inject.Inject;

import static utils.MessageKey.UNEXPECTED_ERROR;

/**
 * @author rishabh
 */
public class SetupController extends Controller {

    private static final String INVALID_JSON = "Invalid Json.";
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
        Confirmation newData;
        try {
            newData = Json.fromJson(json, Confirmation.class);
            log.debug("Received confirmation: {}", newData.toString());
        } catch (Exception e) {
            log.error("Error in parsing the confirmation data!", e);
            log.error("Received the following data: {}", json.toString());
            return badRequest(INVALID_JSON);
        }

        if (isInvalid(newData)) {
            return badRequest(INVALID_JSON);
        }

        String userId = newData.getUser().getId();
        Confirmation oldData = confirmationDao.getByUserId(userId);
        if (null != oldData) {
            log.debug("Found an existing entry for user {}", userId);
            copyOverData(newData, oldData);
        } else {
            log.debug("Brand new user {}!", userId);
            oldData = newData;
        }

        WriteResult result = confirmationDao.insert(oldData);
        if (result.wasAcknowledged()) {
            final String upsertedId;
            if (result.isUpdateOfExisting()) {
                upsertedId = oldData.getId().toString();
                log.info("Confirmation {} updated", upsertedId);
            } else {
                upsertedId = result.getUpsertedId().toString();
                log.info("Confirmation {} created", upsertedId);
            }
            authorizeCall.setId(upsertedId);
            return status(333).withHeader("Location", authorizeCall.url());
        } else {
            log.error("Error while persisting confirmation. {}", result.toString());
            return internalServerError(getMsg().at(UNEXPECTED_ERROR));
        }
    }

    private boolean isInvalid(Confirmation newData) {
        if (!StringUtils.hasText(newData.getPluginId()))
            return true;
        if (null == newData.getUser())
            return true;
        if (!StringUtils.hasText(newData.getUser().getId()))
            return true;
        return false;
    }

    private void copyOverData(Confirmation source, Confirmation destination) {
        destination.setPluginId(source.getPluginId());
        if (null != destination.getUser()) {
            destination.getUser().setName(source.getUser().getName());
            destination.getUser().setCompany(source.getUser().getCompany());
            destination.getUser().setEmail(source.getUser().getEmail());
        } else {
            destination.setUser(source.getUser());
        }
    }

    private Messages getMsg() {
        return messagesApi.preferred(request());
    }
}
