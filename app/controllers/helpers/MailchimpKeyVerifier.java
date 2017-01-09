package controllers.helpers;

import models.BaseValue;
import models.Installation;
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
import utils.InstallationUtility;
import utils.MessageKey;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.INTERNAL_SERVER_ERROR;
import static play.mvc.Results.*;

/**
 * @author rishabh
 */
public class MailchimpKeyVerifier {

    private final Logger.ALogger log = Logger.of(MailchimpKeyVerifier.class);

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
    Result verifyAndCompleteInstallation(Installation installation, Messages messages) {
        Map<String, List<BaseValue>> prefsMap = new HashMap<>();
        installation.getPreferences().forEach(preference -> prefsMap.put(preference.getKey(), preference.getValues()));
        if (prefsMap.isEmpty() || null == prefsMap.get(Constants.CONFIG_MC_API_KEY)) {
            log.error("Mailchimp API key not provided as part of installation initiation");
            return returnErrorMailchimpApiKeyNotProvided(messages);
        }
        String apiKeyWithDataCentre = (String) prefsMap.get(Constants.CONFIG_MC_API_KEY).get(0).getLabel();
        if (StringUtils.hasText(apiKeyWithDataCentre) && apiKeyWithDataCentre.indexOf('-') > 0) {
            String[] meta = apiKeyWithDataCentre.split("-");
            String apiKey = meta[0];
            String dataCentre = meta[1];
            if (StringUtils.hasText(apiKey) && StringUtils.hasText(dataCentre)) {
                log.info("Verifying API key {} for installation {}", apiKey, installation.getId().toHexString());
                try {
                    WSResponse response = ws.url(String.format(appConfig.getString(Constants.MAILCHIMP_VERIFY_URL), dataCentre))
                            .setAuth("username", apiKey).get().toCompletableFuture().get();
                    if (response.getStatus() == Http.Status.OK) {
                        log.info("Mailchimp API key is verified for installation {}", installation.getId().toHexString());
                        return ok(messages.at(MessageKey.VALIDATIONS_PASSED));
                    } else {
                        log.info("The provided Mailchimp API key doesn't seems to be valid one. Installation {}",
                                installation.getId().toHexString());
                        return returnErrorInvalidMailchimpApiKey(messages);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error connecting to mailchimp...", e);
                    return internalServerError(ErrorUtil.toJson(INTERNAL_SERVER_ERROR, messages.at(MessageKey.UNEXPECTED_ERROR)));
                }
            } else {
                log.error("Invalid Mailchimp API key provided.");
                return returnErrorInvalidMailchimpApiKey(messages);
            }
        } else {
            log.error("Mailchimp API key is null/empty/invalid for installation {}", installation.getId().toHexString());
            return returnErrorInvalidMailchimpApiKey(messages);
        }
    }

    private Result returnErrorMailchimpApiKeyNotProvided(Messages messages) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put(Constants.CONFIG_MC_API_KEY, messages.at(MessageKey.MC_API_KEY_MISSING));
        return badRequest(ErrorUtil.toJson(BAD_REQUEST, messages.at(MessageKey.VALIDATION_ERRORS), errorMap));
    }

    private Result returnErrorInvalidMailchimpApiKey(Messages messages) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put(Constants.CONFIG_MC_API_KEY, messages.at(MessageKey.MC_API_KEY_INVALID));
        return badRequest(ErrorUtil.toJson(BAD_REQUEST, messages.at(MessageKey.VALIDATION_ERRORS), errorMap));
    }
}
