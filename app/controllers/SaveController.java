package controllers;

import com.mongodb.WriteResult;
import daos.ConfirmationDao;
import models.Confirmation;
import models.MailingList;
import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;
import play.Configuration;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.inject.ConfigurationProvider;
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

    private static final String INSTALLATION_URL = "xola.installation.url";
    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY = "xola.api.key";
    private final Logger.ALogger log = Logger.of(SaveController.class);

    private final FormFactory formFactory;
    private final WSClient ws;
    private final ConfirmationDao confirmationDao;
    private final Configuration configuration;

    @Inject
    public SaveController(FormFactory formFactory, WSClient ws, ConfirmationDao confirmationDao,
                          ConfigurationProvider configurationProvider) {
        this.formFactory = formFactory;
        this.ws = ws;
        this.confirmationDao = confirmationDao;
        this.configuration = configurationProvider.get();
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
            return finalizeConfiguration(confirmation);
        } else {
            log.error("Error while updating the selected mailing list to db.");
            return CompletableFuture.completedFuture(internalServerError("Error saving the selection."));
        }
    }

    private CompletionStage<Result> finalizeConfiguration(Confirmation confirmation) {
        return ws.url(String.format(configuration.getString(INSTALLATION_URL), confirmation.getPluginId(),
                confirmation.getUser().getId()))
                .setHeader(API_KEY_HEADER, configuration.getString(API_KEY))
                .post("")
                .thenApply(wsResponse -> {
                    if (wsResponse.getStatus() == Http.Status.CREATED || wsResponse.getStatus() == Http.Status.OK) {
                        return redirect(controllers.routes.FinalController.index("success"));
                    } else if (wsResponse.getStatus() == Http.Status.CONFLICT) {
                        return redirect(controllers.routes.FinalController.index("exists"));
                    } else {
                        return redirect(controllers.routes.FinalController.index("error"));
                    }
                });
    }

}
