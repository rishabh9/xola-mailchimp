package controllers;

import controllers.helpers.MailingListHelper;
import daos.InstallationDao;
import models.Installation;
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
    public CompletionStage<Result> getLists() {
        log.info("Request made to retrieve the mailings lists... as json");
        Messages messages = messagesApi.preferred(request());
        DynamicForm requestData = formFactory.form().bindFromRequest();
        String installationId = requestData.get("installationId");
        if (!StringUtils.hasText(installationId)) {
            log.debug("Error validating the request parameters... Missing installation id");
            return CompletableFuture.completedFuture(
                    badRequest(Errors.toJson(BAD_REQUEST, messages.at(MessageKey.INVALID_PARAM_I))));
        }
        log.info("Requesting mailing list for installation {}", installationId);
        Optional<Installation> installation = installationDao.getByInstallationId(installationId);
        if (installation.isPresent()) {
            return helper.getMailingListsAsJson(installation.get());
        } else {
            log.debug("Didn't find the confirmation object having installationId: {}", installationId);
            return CompletableFuture.completedFuture(
                    notFound(Errors.toJson(NOT_FOUND, messages.at(MessageKey.NOT_FOUND))));
        }
    }
}
