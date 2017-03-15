package controllers.helpers;

import models.Installation;
import models.Value;
import org.springframework.util.StringUtils;
import play.Configuration;
import play.Logger;
import play.i18n.Messages;
import play.inject.ConfigurationProvider;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import play.mvc.Result;
import utils.Constants;
import utils.ErrorUtil;
import utils.MessageKey;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.INTERNAL_SERVER_ERROR;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.internalServerError;

/**
 * @author rishabh
 */
public class MailchimpKeyVerifier {

    private final Logger.ALogger log = Logger.of(MailchimpKeyVerifier.class);

    private final WSClient ws;
    private final Configuration appConfig;

    @Inject
    public MailchimpKeyVerifier(WSClient ws, ConfigurationProvider configProvider) {
        this.ws = ws;
        this.appConfig = configProvider.get();
    }

    /**
     * Verify we have all the data and complete installation.
     *
     * @param installation
     * @return
     */
    Optional<Result> verifyAndCompleteInstallation(Installation installation, Messages messages) {
        Map<String, List<Value>> prefsMap = new HashMap<>();
        installation.getPreferences().forEach(preference -> prefsMap.put(preference.getKey(), preference.getValues()));
        if (prefsMap.isEmpty() || null == prefsMap.get(Constants.CONFIG_MC_API_KEY)) {
            log.error("Mailchimp API key not provided as part of installation initiation");
            return returnMailchimpApiKeyError(messages, MessageKey.MC_API_KEY_MISSING);
        }
        String apiKeyWithDataCentre = (String) prefsMap.get(Constants.CONFIG_MC_API_KEY).get(0).getLabel();
        if (StringUtils.hasText(apiKeyWithDataCentre) && apiKeyWithDataCentre.indexOf('-') > 0) {
            String[] meta = apiKeyWithDataCentre.split("-");
            String apiKey = meta[0];
            String dataCentre = meta[1];
            if (StringUtils.hasText(apiKey) && StringUtils.hasText(dataCentre)) {
                log.info("Verifying API key {} for installation {}", apiKey, installation.getInstallationId());
                try {
                    WSResponse response = ws.url(String.format(appConfig.getString(Constants.MAILCHIMP_VERIFY_URL), dataCentre))
                            .setAuth("username", apiKey).get().toCompletableFuture().get();
                    if (response.getStatus() == Http.Status.OK) {
                        log.info("Mailchimp API key is verified for installation {}", installation.getInstallationId());
                        return Optional.empty();
                    } else {
                        log.info("The provided Mailchimp API key doesn't seems to be valid one. Installation {}",
                                installation.getId().toHexString());
                        return returnMailchimpApiKeyError(messages, MessageKey.MC_API_KEY_INVALID);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error connecting to mailchimp...", e);
                    return Optional.of(
                            internalServerError(
                                    ErrorUtil.toJson(INTERNAL_SERVER_ERROR, messages.at(MessageKey.UNEXPECTED_ERROR))));
                }
            } else {
                log.error("Invalid Mailchimp API key provided.");
                return returnMailchimpApiKeyError(messages, MessageKey.MC_API_KEY_INVALID);
            }
        } else {
            log.error("Mailchimp API key is null/empty/invalid for installation {}", installation.getId().toHexString());
            return returnMailchimpApiKeyError(messages, MessageKey.MC_API_KEY_INVALID);
        }
    }

    private Optional<Result> returnMailchimpApiKeyError(Messages messages, String errorKey) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put(Constants.CONFIG_MC_API_KEY, messages.at(errorKey));
        return Optional.of(
                badRequest(
                        ErrorUtil.toJson(BAD_REQUEST, messages.at(MessageKey.VALIDATION_ERRORS), errorMap)));
    }
}
