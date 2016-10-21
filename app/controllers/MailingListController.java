package controllers;

import controllers.helpers.MailingListHelper;
import daos.InstallationDao;
import models.Installation;
import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.ws.WSClient;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Errors;
import utils.MessageKey;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author rishabh
 */
public class MailingListController extends Controller {

    private final Logger.ALogger log = Logger.of(MailingListController.class);

    private final WSClient ws;
    private final InstallationDao installationDao;
    private final MessagesApi messagesApi;
    private final FormFactory formFactory;
    private final MailingListHelper helper;

    @Inject
    public MailingListController(WSClient ws, InstallationDao installationDao, MessagesApi messagesApi,
                                 FormFactory formFactory, MailingListHelper helper) {
        this.ws = ws;
        this.installationDao = installationDao;
        this.messagesApi = messagesApi;
        this.formFactory = formFactory;
        this.helper = helper;
    }

    @BodyParser.Of(BodyParser.FormUrlEncoded.class)
    public CompletionStage<Result> getFieldData(String installationId, String field) {
        log.info("Request made to retrieve the mailings lists... as json");
        Messages messages = messagesApi.preferred(request());
        List<String> errors = validate(installationId, field, messages);
        if (!errors.isEmpty()) {
            log.debug("Error validating the request parameters...");
            return CompletableFuture.completedFuture(badRequest(Errors.toJson(BAD_REQUEST, errors)));
        }
        log.info("Requesting mailing list for installation {}", installationId);
        Optional<Installation> confirmation = installationDao.getByInstallationId(installationId);
        if (confirmation.isPresent()) {
            return helper.getMailingListsAsJson(confirmation.get());
        } else {
            log.debug("Didn't find the confirmation object having installationId: {}", installationId);
            return CompletableFuture.completedFuture(
                    notFound(Errors.toJson(NOT_FOUND, messages.at(MessageKey.NOT_FOUND))));
        }
    }

    private List<String> validate(String installationId, String field, Messages messages) {
        List<String> errors = new ArrayList<>();
        if (!StringUtils.hasText(installationId)) {
            errors.add(messages.at(MessageKey.INVALID_PARAM_I));
        }
        if (!StringUtils.hasText(field)) {
            errors.add(messages.at(MessageKey.INVALID_PARAM_U));
        }
        return errors;
    }

    public CompletionStage<Result> index() {
        log.info("Requesting mailing lists ... as HTML");
        DynamicForm requestData = formFactory.form().bindFromRequest();
        String confirmationId = requestData.get("confirm");
        if (!StringUtils.hasText(confirmationId)) {
            confirmationId = flash("confirm");
        }
        if (StringUtils.hasText(confirmationId)) {
            log.info("Retrieving mailing lists for installation {}", confirmationId);
            Installation installation = installationDao.get(new ObjectId(confirmationId));
            CompletionStage<Result> result = helper.getMailingListsAsHTML(installation);
            return result;
        } else {
            log.error("Invalid ConfirmationId: {}", confirmationId);
            return CompletableFuture.completedFuture(badRequest());
        }
    }
}