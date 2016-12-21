package controllers.helpers;

import com.mongodb.WriteResult;
import daos.InstallationDao;
import models.Installation;
import models.Preference;
import models.payload.Data;
import org.springframework.util.StringUtils;
import play.Logger;
import play.i18n.Messages;
import play.mvc.Result;
import utils.ErrorUtil;
import utils.MessageKey;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static play.mvc.Http.Status.*;
import static play.mvc.Results.*;

/**
 * @author rishabh
 */
public class ConfigUpdateHelper {

    private final transient Logger.ALogger log = Logger.of(ConfigUpdateHelper.class);

    private final InstallationDao installationDao;

    @Inject
    public ConfigUpdateHelper(InstallationDao installationDao) {
        this.installationDao = installationDao;
    }

    /**
     * Update the plugin configuration provided by Xola for the given installation id.
     *
     * @param data     Payload
     * @param messages Messages API object for i18n.
     * @return The installation's configuration
     */
    public Result updateConfiguration(Data data, Messages messages) {
        log.info("Received request to update the plugin configuration for installation {}", data.getId());
        if (!StringUtils.hasText(data.getId())) {
            log.debug("Invalid installation id provided: {}", data.getId());
            return badRequest(ErrorUtil.toJson(BAD_REQUEST, messages.at(MessageKey.MISSING_INSTALL_ID)));
        }
        Optional<Installation> installation = installationDao.getByInstallationId(data.getId());
        if (!installation.isPresent()) {
            log.debug("Installation with the given installation id was not found: {}", data.getId());
            return notFound(ErrorUtil.toJson(NOT_FOUND, messages.at(MessageKey.NOT_FOUND)));
        }
        if (data.getPreferences().isEmpty()) {
            log.debug("Missing config values");
            badRequest(ErrorUtil.toJson(BAD_REQUEST, messages.at(MessageKey.INVALID_JSON)));
        }
        Installation confirm = installation.get();
        List<Preference> prefs = confirm.getPreferences();
        prefs.addAll(data.getPreferences());
        confirm.setPreferences(prefs);
        WriteResult result = installationDao.insert(confirm);
        if (result.wasAcknowledged()) {
            log.debug("Configuration saved successfully for installation {}", data.getId());
            return ok(messages.at(MessageKey.CONFIG_UPDATED));
        } else {
            log.debug("Error saving configuration for installation {}", data.getId());
            return internalServerError(ErrorUtil.toJson(INTERNAL_SERVER_ERROR, MessageKey.UNEXPECTED_ERROR));
        }
    }
}
