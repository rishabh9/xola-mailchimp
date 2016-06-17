package controllers;

import com.mongodb.WriteResult;
import controllers.helpers.MailChimpAuthorizeCall;
import daos.ConfirmationDao;
import models.Confirmation;
import org.springframework.util.StringUtils;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

import static utils.MessageKey.UNEXPECTED_ERROR;

/**
 * @author rishabh
 */
public class ConfirmationController extends Controller {

    private final Logger.ALogger log = Logger.of(ConfirmationController.class);

    private final ConfirmationDao confirmationDao;
    private final MessagesApi messagesApi;
    private final MailChimpAuthorizeCall authorizeCall;
    private final FormFactory formFactory;

    @Inject
    public ConfirmationController(final ConfirmationDao confirmationDao, final MessagesApi messagesApi,
                                  final MailChimpAuthorizeCall authorizeCall, final FormFactory formFactory) {
        super();
        this.confirmationDao = confirmationDao;
        this.messagesApi = messagesApi;
        this.authorizeCall = authorizeCall;
        this.formFactory = formFactory;
    }

    @BodyParser.Of(BodyParser.FormUrlEncoded.class)
    public Result confirm() {
        final DynamicForm requestData = formFactory.form().bindFromRequest();
        final String uid = requestData.get("uid");
        final String timestamp = requestData.get("ts");
        if (!StringUtils.hasText(uid) || !StringUtils.hasText(timestamp)) {
            log.error("Error in parsing the confirmation data! Received: {} & {}", uid, timestamp);
            return badRequest();
        }
        log.debug("Received installation confirmation from Xola: UID: {}, TS: {}", uid, timestamp);

        final WriteResult result = confirmationDao.insert(new Confirmation(uid, timestamp));
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
