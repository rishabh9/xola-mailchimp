package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import daos.ConfirmationDao;
import models.Confirmation;
import models.MailingList;
import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author rishabh
 */
public class ListController extends Controller {

    private final Logger.ALogger log = Logger.of(ListController.class);

    private final WSClient ws;
    private final FormFactory formFactory;
    private final ConfirmationDao confirmationDao;

    @Inject
    public ListController(WSClient ws, FormFactory formFactory, ConfirmationDao confirmationDao) {
        this.ws = ws;
        this.formFactory = formFactory;
        this.confirmationDao = confirmationDao;
    }

    public CompletionStage<Result> index() {
        DynamicForm requestData = formFactory.form().bindFromRequest();
        String confirmationId = requestData.get("confirm");
        if (!StringUtils.hasText(confirmationId)) {
            confirmationId = flash("confirm");
        }
        if (StringUtils.hasText(confirmationId)) {
            log.info("Retrieving mailing lists for confirmation {}", confirmationId);
            Confirmation confirmation = confirmationDao.get(new ObjectId(confirmationId));
            WSRequest request = ws.url(confirmation.getMetadata().getApiEndpoint() + "/3.0/lists")
                    .setHeader(Http.HeaderNames.ACCEPT, Http.MimeTypes.JSON)
                    .setHeader(Http.HeaderNames.CONTENT_TYPE, Http.MimeTypes.JSON)
                    .setHeader(Http.HeaderNames.AUTHORIZATION, "Bearer " + confirmation.getAccessToken());

            CompletionStage<Result> result = request.get()
                    .thenApply(wsResponse -> getLists(wsResponse, confirmation));
            return result;
        } else {
            log.error("Invalid ConfirmationId: {}", confirmationId);
            return CompletableFuture.completedFuture(badRequest());
        }
    }


    private Result getLists(WSResponse wsResponse, Confirmation confirmation) {
        log.debug("Getting mailing lists for confirmation {} was a success", confirmation.getId().toString());
        JsonNode jsonNode = wsResponse.asJson();
        JsonNode arr = jsonNode.get("lists");
        List<MailingList> lists = new ArrayList<>();
        if (arr.isArray()) {
            log.debug("Count of available mailing lists: {}", arr.size());
            for (int i = 0; i < arr.size(); i++) {
                MailingList list = new MailingList();
                JsonNode node = arr.get(i);
                list.setId(node.get("id").asText());
                list.setName(node.get("name").asText());
                lists.add(list);
            }
            MailingList list = confirmation.getList();
            String selectedId = "";
            if (null != list) {
                selectedId = confirmation.getList().getId();
            }
            log.debug("Rendering view with {} items in list and selected item is '{}'", arr.size(), selectedId);
            return ok(views.html.lists.render(lists, selectedId, confirmation.getId().toString()));
        } else {
            log.debug("Received empty mailing list for confirmation {}", confirmation.getId().toString());
            return ok(views.html.lists.render(lists, "", confirmation.getId().toString()));
        }
    }
}
