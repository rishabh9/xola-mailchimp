package controllers.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import models.Installation;
import models.MailingList;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;

/**
 * @author rishabh
 */
@Singleton
public final class MailingListHelper {

    private final Logger.ALogger log = Logger.of(MailingListHelper.class);

    private final WSClient ws;

    @Inject
    public MailingListHelper(WSClient ws) {
        this.ws = ws;
    }

    public CompletionStage<Result> getMailingListsAsJson(Installation installation) {
        return request(installation).get()
                .thenApply(wsResponse -> getListsForJson(wsResponse, installation));
    }

    public CompletionStage<Result> getMailingListsAsHTML(Installation installation) {
        return request(installation).get()
                .thenApply(wsResponse -> getListsForHTML(wsResponse, installation));
    }

    private WSRequest request(Installation installation) {
        return ws.url(installation.getMetadata().getApiEndpoint() + "/3.0/lists")
                .setHeader(Http.HeaderNames.ACCEPT, Http.MimeTypes.JSON)
                .setHeader(Http.HeaderNames.CONTENT_TYPE, Http.MimeTypes.JSON)
                .setHeader(Http.HeaderNames.AUTHORIZATION, "Bearer " + installation.getAccessToken());
    }


    private Result getListsForJson(WSResponse wsResponse, Installation installation) {
        log.debug("Getting mailing lists for installation {} was a success", installation.getId().toString());
        List<MailingList> lists = getMailingLists(wsResponse);
        if (lists.isEmpty()) {
            log.debug("Received empty mailing list for installation {}", installation.getId().toString());
            return ok(Json.newArray());
        } else {
            ArrayNode array = Json.newArray();
            lists.forEach(mailingList -> array.add(
                    Json.newObject().put("key", mailingList.getId()).put("value", mailingList.getName())));
            String selectedId = getSelectedValue(installation);
            log.debug("Returning {} items with default as {}", array.size(), selectedId);
            return ok(Json.newObject().put("default", selectedId).set("values", array));
        }
    }

    private Result getListsForHTML(WSResponse wsResponse, Installation installation) {
        log.debug("Getting mailing lists for installation {} was a success", installation.getId().toString());
        List<MailingList> lists = getMailingLists(wsResponse);
        if (lists.isEmpty()) {
            log.debug("Received empty mailing list for installation {}", installation.getId().toString());
            return ok(views.html.lists.render(lists, "", installation.getId().toString()));
        } else {
            String selectedId = getSelectedValue(installation);
            log.debug("Rendering view with {} items in list and selected item is '{}'", lists.size(), selectedId);
            return ok(views.html.lists.render(lists, selectedId, installation.getId().toString()));
        }
    }

    private String getSelectedValue(Installation installation) {
        MailingList list = installation.getList();
        String selectedId = "";
        if (null != list) {
            selectedId = installation.getList().getId();
        }
        return selectedId;
    }

    private List<MailingList> getMailingLists(WSResponse wsResponse) {
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
        }
        return lists;
    }
}
