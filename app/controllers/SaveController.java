package controllers;

import com.mongodb.WriteResult;
import daos.ConfirmationDao;
import models.Confirmation;
import models.MailingList;
import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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

    public CompletionStage<Result> index() {
        log.info("Saving the selected mailing list...");
        DynamicForm requestData = formFactory.form().bindFromRequest();
        String id = requestData.get("id");
        String name = requestData.get("name");
        String confirmationId = requestData.get("confirm");
        log.debug("Received id: {}, name: {}, confirmationId: {}", id, name, confirmationId);
        if (!StringUtils.hasText(confirmationId)) {
            log.error("Invalid system state. Invalid confirmation id received: '{}'", confirmationId);
            return CompletableFuture.completedFuture(badRequest("Invalid system state."));
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
            log.info("Plugin is setup successfully for confirmation {}", confirmationId);
            return CompletableFuture.supplyAsync(this::confirmInstallationWithXola)
                    .thenApply((Integer status) -> {
                        if (status == Http.Status.CREATED || status == Http.Status.OK) {
                            return redirect(controllers.routes.FinalController.index("success"));
                        } else {
                            return redirect(controllers.routes.FinalController.index("error"));
                        }
                    });
        } else {
            log.error("Error while updating the selected mailing list to db.");
            return CompletableFuture.completedFuture(internalServerError("Error saving the selection."));
        }
    }

    private Integer confirmInstallationWithXola() {
        // TODO: Call the Xola App Store for installation confirmation.
        //CompletionStage<WSResponse> response = ws.url("").setHeader("","").post("");
        return Http.Status.INTERNAL_SERVER_ERROR;
    }
}
