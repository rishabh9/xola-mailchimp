package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.WriteResult;
import daos.ConfirmationDao;
import models.Confirmation;
import models.Metadata;
import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;
import play.Configuration;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.inject.ConfigurationProvider;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author rishabh
 */
public class OAuthController extends Controller {

    private static final String ACCESS_TOKEN_URL = "mailchimp.access.token.url";
    private static final String ACCESS_TOKEN_CONTENT_TYPE = "mailchimp.access.token.content.type";
    private static final String CLIENT_ID = "mailchimp.client.id";
    private static final String CLIENT_SECRET = "mailchimp.client.secret";
    private static final String REDIRECT_URI = "mailchimp.redirect.uri";
    private static final String MAILCHIMP_METADATA_URL = "mailchimp.metadata.url";

    private final Logger.ALogger log = Logger.of(OAuthController.class);

    private final FormFactory formFactory;
    private final WSClient ws;
    private final ConfirmationDao confirmationDao;
    private final Configuration configuration;

    @Inject
    public OAuthController(FormFactory formFactory, WSClient ws, ConfirmationDao confirmationDao,
                           ConfigurationProvider configProvider) {
        super();
        this.formFactory = formFactory;
        this.ws = ws;
        this.confirmationDao = confirmationDao;
        this.configuration = configProvider.get();
    }

    public CompletionStage<Result> complete() {
        DynamicForm requestData = formFactory.form().bindFromRequest();
        String code = requestData.get("code");
        String confirmationId = requestData.get("confirm");
        log.debug("OAuth2 Code: {}, ConfirmationId: {}", code, confirmationId);

        Confirmation confirmation = getConfirmation(confirmationId);
        if (null == confirmation) {
            // This situation should not happen unless someone is accessing it from outside.
            log.error("Received invalid confirmationId {}", confirmationId);
            return CompletableFuture.completedFuture(badRequest());
        }
        confirmation.setOauthCode(code);
        updateConfirmation(confirmation);
        return requestAccessToken(code, confirmation);
    }

    private CompletionStage<Result> requestAccessToken(String code, Confirmation confirmation) {
        String confirmId = confirmation.getId().toString();
        String data = "grant_type=authorization_code"
                + "&client_id=" + configuration.getString(CLIENT_ID)
                + "&client_secret=" + configuration.getString(CLIENT_SECRET)
                + "&code=" + code
                + "&redirect_uri=" + configuration.getString(REDIRECT_URI)
                // Need to append this below because it was used to create the OAuth Code.
                // Else the Access Token is not created.
                + "?confirm=" + confirmId;
        WSRequest request = ws.url(configuration.getString(ACCESS_TOKEN_URL))
                .setContentType(configuration.getString(ACCESS_TOKEN_CONTENT_TYPE));
        return request.post(data)
                .thenCompose(wsResponse -> {
                    JsonNode jsonNode = wsResponse.asJson();
                    if (null != jsonNode && jsonNode.path("access_token").isMissingNode()) {
                        log.error("Error making access token request. {}", jsonNode.toString());
                        return null;
                    } else {
                        String access_token = jsonNode.path("access_token").textValue();
                        saveAccessToken(confirmation, confirmId, access_token);
                        log.debug("Retrieving metadata for confirmation {}", confirmId);
                        return ws.url(configuration.getString(MAILCHIMP_METADATA_URL))
                                .setHeader(Http.HeaderNames.ACCEPT, Http.MimeTypes.JSON)
                                .setHeader(Http.HeaderNames.AUTHORIZATION, "Bearer " + access_token)
                                .get();
                    }
                })
                .thenApply(wsResponse -> {
                    if (null != wsResponse) {
                        JsonNode jsonNode = wsResponse.asJson();
                        if (jsonNode.path("dc").isMissingNode()) {
                            log.error("Error retrieving metadata for confirmation {}", confirmId);
                            return internalServerError();
                        } else {
                            Metadata meta = new Metadata();
                            meta.setApiEndpoint(jsonNode.path("api_endpoint").asText());
                            meta.setDc(jsonNode.path("dc").asText());
                            meta.setLoginUrl(jsonNode.path("login_url").asText());
                            confirmation.setMetadata(meta);
                            log.debug("Saving metadata for confirmation {}", confirmId);
                            updateConfirmation(confirmation);
                            log.debug("Redirect to available lists page...");
                            return redirect("/lists?confirm="+confirmId);
                        }
                    } else {
                        return internalServerError();
                    }
                });
    }

    private String saveAccessToken(Confirmation confirmation, String confirmId, String access_token) {
        log.debug("Access token {} for Confirmation {}", access_token, confirmId);
        confirmation.setAccessToken(access_token);
        updateConfirmation(confirmation);
        return access_token;
    }

    private void updateConfirmation(Confirmation confirmation) {
        log.debug("Updating confirmation id {}", confirmation.getId().toString());
        WriteResult result = confirmationDao.insert(confirmation);
        if (result.wasAcknowledged()) {
            log.debug("Update {}", result.isUpdateOfExisting());
        } else {
            log.error("Error updating db = {}", result.toString());
        }
    }

    private Confirmation getConfirmation(String confirmationId) {
        if (StringUtils.hasText(confirmationId)) {
            try {
                return confirmationDao.get(new ObjectId(confirmationId));
            } catch (IllegalArgumentException e) {
                log.error("Invalid Id", e);
                return null;
            }
        }
        return null;
    }

}
