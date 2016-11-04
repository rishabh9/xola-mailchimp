package controllers.helpers;

import models.Installation;
import org.springframework.util.StringUtils;
import play.Configuration;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author rishabh
 */
public class MailchimpKeyVerifier {

    private final Logger.ALogger log = Logger.of(MailchimpKeyVerifier.class);

    private static final String INSTALLATION_URL = "xola.installation.url";
    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY = "xola.api.key";

    private final WSClient ws;
    private final Configuration appConfig;

    @Inject
    public MailchimpKeyVerifier(WSClient ws, Configuration appConfig) {
        this.ws = ws;
        this.appConfig = appConfig;
    }

    /**
     * Verify we have all the data and complete installation.
     *
     * @param installation
     * @return
     */
    boolean verifyAndCompleteInstallation(Installation installation) {

        // Verify the configuration has 'mc-api-key'
        if (null == installation.getConfigValues() || installation.getConfigValues().isEmpty()) {
            // If not present, fail silently. Do not make complete call.
            log.debug("Configuration is missing or empty.");
        } else {
            installation.getConfigValues().forEach(config -> {
                if (config.getKey().equals("mc-api-key")) {
                    // Make complete call to App store.
                    verifyApiKey(installation, config.getValue())
                            .whenComplete((wsResponse, throwable) -> {
                                if (null != throwable) {
                                    log.error("Error talking to Mailchimp API", throwable);
                                }
                                if (null != wsResponse && wsResponse.getStatus() == Http.Status.OK) {
                                    completeInstallation(installation);
                                }
                            });
                }
            });
        }
        return true;
    }

    private CompletionStage<WSResponse> verifyApiKey(Installation installation, String apiKey) {
        log.info("Verifying API key {} for installation {}", apiKey, installation.getId().toHexString());
        if (StringUtils.hasText(apiKey)) {
            String[] meta = apiKey.split("-");
            if (null != meta && meta.length == 2) {
                return ws.url(String.format(appConfig.getString("mailchimp.verify.url"), meta[1]))
                        .setAuth("username", meta[0])
                        .get();
            } else {
                log.warn("The API key '{}' doesn't have key and data center information. Cannot proceed.", apiKey);
                return CompletableFuture.completedFuture(null);
            }
        } else {
            log.warn("The API key is empty! Cannot proceed.");
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletionStage<WSResponse> completeInstallation(Installation installation) {
        log.info("Making call to Xola App Store to complete installation for {}", installation.getId().toHexString());
        return ws.url(String.format(appConfig.getString(INSTALLATION_URL), installation.getInstallationId()))
                .setHeader(API_KEY_HEADER, appConfig.getString(API_KEY))
                .put("")
                .whenComplete((wsResponse, throwable) -> {
                    if (null != throwable) {
                        log.error("Error talking to Xola App Store API", throwable);
                    }
                    if (null != wsResponse && wsResponse.getStatus() == Http.Status.OK) {
                        log.info("Received HTTP status {} for installation {}.", installation.getId().toHexString());
                    }
                });
    }
}
