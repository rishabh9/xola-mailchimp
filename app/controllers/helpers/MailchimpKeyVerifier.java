package controllers.helpers;

import models.Installation;
import play.Configuration;
import play.Logger;
import play.inject.ConfigurationProvider;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import utils.Constants;
import utils.InstallationUtility;

import javax.inject.Inject;
import java.util.Optional;
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
    private final InstallationUtility utility;

    @Inject
    public MailchimpKeyVerifier(WSClient ws, ConfigurationProvider configProvider,
                                InstallationUtility utility) {
        this.ws = ws;
        this.appConfig = configProvider.get();
        this.utility = utility;
    }

    /**
     * Verify we have all the data and complete installation.
     *
     * @param installation
     * @return
     */
    boolean verifyAndCompleteInstallation(Installation installation) {

        // Verify the configuration has 'mc-api-key'
        Optional<String> apiKey = utility.getApiKey(installation);
        Optional<String> dataCentre = utility.getDataCentre(installation);
        if (apiKey.isPresent() && dataCentre.isPresent()) {
            verifyApiKey(installation, apiKey.get(), dataCentre.get())
                    .whenComplete((wsResponse, throwable) -> {
                        if (null != throwable) {
                            log.error("Error talking to Mailchimp API", throwable);
                        }
                        if (null != wsResponse && wsResponse.getStatus() == Http.Status.OK) {
                            completeInstallation(installation);
                        }
                    });
        } else {
            log.warn("Inconsistent data ApiKey present? {}, DataCentre present? {}",
                    apiKey.isPresent(), dataCentre.isPresent());
        }
        return true;
    }

    private CompletionStage<WSResponse> verifyApiKey(Installation installation, String apiKey, String dataCentre) {
        log.info("Verifying API key {} for installation {}", apiKey, installation.getId().toHexString());
        return ws.url(
                String.format(
                        appConfig.getString(Constants.MAILCHIMP_VERIFY_URL),
                        dataCentre))
                .setAuth("username", apiKey)
                .get();
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
