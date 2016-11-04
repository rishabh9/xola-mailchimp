package controllers.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import models.Installation;
import models.MailingList;
import play.Configuration;
import play.Logger;
import play.inject.ConfigurationProvider;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import play.mvc.Result;
import utils.InstallationUtility;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;
import static utils.Constants.MAILCHIMP_GET_LISTS_URL;

/**
 * @author rishabh
 */
@Singleton
public final class MailingListHelper {

    private final Logger.ALogger log = Logger.of(MailingListHelper.class);

    private final WSClient ws;
    private final Configuration config;
    private final InstallationUtility utility;

    @Inject
    public MailingListHelper(WSClient ws, ConfigurationProvider configProvider,
                             InstallationUtility utility) {
        this.ws = ws;
        this.config = configProvider.get();
        this.utility = utility;
    }

    public CompletionStage<Result> getMailingListsAsJson(Installation installation) {

        Optional<String> apiKey = utility.getApiKey(installation);
        Optional<String> dataCentre = utility.getDataCentre(installation);
        if (apiKey.isPresent() && dataCentre.isPresent()) {
            return ws.url(String.format(
                    config.getString(MAILCHIMP_GET_LISTS_URL), dataCentre.get()))
                    .setHeader(Http.HeaderNames.ACCEPT, Http.MimeTypes.JSON)
                    .setHeader(Http.HeaderNames.CONTENT_TYPE, Http.MimeTypes.JSON)
                    .setAuth("username", apiKey.get())
                    .get()
                    .thenApply(wsResponse -> getListsForJson(wsResponse, installation));
        } else {
            return CompletableFuture.completedFuture(okResponse(Json.newArray(), ""));
        }
    }

    private Result getListsForJson(WSResponse wsResponse, Installation installation) {
        log.debug("Getting mailing lists for installation {} was a success", installation.getId().toString());
        List<MailingList> lists = getMailingLists(wsResponse);
        if (lists.isEmpty()) {
            log.debug("Received empty mailing list for installation {}", installation.getId().toString());
            return okResponse(Json.newArray(), "");
        } else {
            ArrayNode array = Json.newArray();
            lists.forEach(mailingList -> array.add(
                    Json.newObject().put("key", mailingList.getId()).put("value", mailingList.getName())));
            String selectedId = getSelectedValue(installation);
            log.debug("Returning {} items with default as {}", array.size(), selectedId);
            return okResponse(array, selectedId);
        }
    }

    private Result okResponse(ArrayNode array, String selectedId) {
        return ok(Json.newObject().put("default", selectedId).set("values", array));
    }

    private String getSelectedValue(Installation installation) {
        Optional<String> configuredListId = utility.getConfiguredListId(installation);
        return configuredListId.isPresent() ? configuredListId.get() : "";
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
