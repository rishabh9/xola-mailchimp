package controllers;

import com.mongodb.WriteResult;
import daos.ConfirmationDao;
import models.Confirmation;
import models.MailingList;
import models.Metadata;
import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

/**
 * @author rishabh
 */
public class SaveController extends Controller {

    private final Logger.ALogger log = Logger.of(SaveController.class);

    private final FormFactory formFactory;
    private final WSClient ws;
    private final ConfirmationDao confirmationDao;

    @Inject
    public SaveController(FormFactory formFactory, WSClient ws, ConfirmationDao confirmationDao) {
        this.formFactory = formFactory;
        this.ws = ws;
        this.confirmationDao = confirmationDao;
    }

    public Result index() {
        log.info("Saving the selected mailing list...");
        DynamicForm requestData = formFactory.form().bindFromRequest();
        String id = requestData.get("id");
        String name = requestData.get("name");
        String confirmationId = requestData.get("confirm");
        log.debug("Received id: {}, name: {}, confirmationId: {}", id, name, confirmationId);
        if (!StringUtils.hasText(confirmationId)) {
            log.error("Invalid system state. Invalid confirmation id received: '{}'", confirmationId);
            return badRequest("Invalid system state.");
        }
        Confirmation confirmation = confirmationDao.get(new ObjectId(confirmationId));
        MailingList list = confirmation.getList();
        if (null == list) {
            list = new MailingList();
        }
        list.setId(id);
        list.setName(name);
        confirmation.setList(list);
        WriteResult result = confirmationDao.insert(confirmation);
        if (result.wasAcknowledged()) {
            flash("confirm", confirmation.getId().toString());
            return redirect(controllers.routes.ListController.index());
        } else {
            log.error("Error while updating the selected mailing list to db.");
            return internalServerError("Error saving the selection.");
        }
    }
}
