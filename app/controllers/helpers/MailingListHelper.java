package controllers.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import models.Installation;
import models.MailingList;
import play.Configuration;
import play.Logger;
import play.i18n.Messages;
import play.inject.ConfigurationProvider;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import play.mvc.Result;
import utils.ErrorUtil;
import utils.InstallationUtility;
import utils.MessageKey;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Http.Status.INTERNAL_SERVER_ERROR;
import static play.mvc.Results.internalServerError;
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

    public CompletionStage<Result> getMailingListsAsJson(Installation installation, Messages messages) {

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
            log.error("Error querying mailchimp api key and datacentre for instalaltion {}",
                    installation.getId().toHexString());
            return CompletableFuture.completedFuture(internalServerError(
                    ErrorUtil.toJson(INTERNAL_SERVER_ERROR, messages.at(MessageKey.UNEXPECTED_ERROR))));
        }
    }

    private Result getListsForJson(WSResponse wsResponse, Installation installation) {
        log.debug("Getting mailing lists for installation {} was a success", installation.getId().toString());
        ArrayNode array = Json.newArray();
        if (wsResponse.getStatus() == Http.Status.OK) {
            List<MailingList> lists = getMailingLists(wsResponse);
            if (lists.isEmpty()) {
                log.debug("Received empty mailing list from mailchimp for installation {}",
                        installation.getId().toString());
                return result(array, "");
            } else {
                lists.forEach(mailingList -> array.add(
                        Json.newObject().put("id", mailingList.getId()).put("label", mailingList.getName())));
                String selectedId = getSelectedValue(installation);
                log.debug("Returning {} items with default as {}", array.size(), selectedId);
                return result(array, selectedId);
            }
        } else {
            log.error("Error retrieving list of mailing lists from mailchimp {}, {}, {}",
                    wsResponse.getStatus(), wsResponse.getStatusText(), wsResponse.getBody());
            return result(array, "");
        }
    }

    private Result result(ArrayNode array, String selectedId) {
        return ok(Json.newObject().put("default", selectedId).set("values", array));
    }

    private String getSelectedValue(Installation installation) {
        Optional<String> configuredListId = utility.getConfiguredListId(installation);
        return configuredListId.isPresent() ? configuredListId.get() : "";
    }

    private List<MailingList> getMailingLists(WSResponse wsResponse) {
        JsonNode jsonNode = wsResponse.asJson();
        JsonNode arr = jsonNode.get("lists");
        if (arr.isArray()) {
            List<MailingList> lists = new ArrayList<>(arr.size());
            log.debug("Count of available mailing lists: {}", arr.size());
            for (int i = 0; i < arr.size(); i++) {
                MailingList list = new MailingList();
                JsonNode node = arr.get(i);
                list.setId(node.get("id").asText());
                list.setName(node.get("name").asText());
                lists.add(list);
            }
            return lists;
        } else {
            return Collections.emptyList();
        }
    }
}
