package utils;

import models.ConfigValues;
import models.Installation;
import org.springframework.util.StringUtils;

import javax.inject.Singleton;
import java.util.Optional;

import static utils.Constants.CONFIG_MC_API_KEY;
import static utils.Constants.CONFIG_MC_LIST_ID;

/**
 * @author rishabh
 */
@Singleton
public class InstallationUtility {

    public Optional<String> getValue(Installation installation, String key) {
        if (null == installation
                || null == installation.getConfigValues()
                || installation.getConfigValues().isEmpty()
                || !StringUtils.hasText(key)) {
            return Optional.empty();
        }
        for (ConfigValues configValues : installation.getConfigValues()) {
            if (key.equals(configValues.getKey()) && StringUtils.hasText(configValues.getValue())) {
                return Optional.of(configValues.getValue());
            }
        }
        return Optional.empty();
    }

    public Optional<String> getConfiguredListId(Installation installation) {
        return getValue(installation, CONFIG_MC_LIST_ID);
    }

    public Optional<String> getDataCentre(Installation installation) {
        return splitApiKey(installation, 1);
    }

    public Optional<String> getApiKey(Installation installation) {
        return splitApiKey(installation, 1);
    }

    private Optional<String> splitApiKey(Installation installation, int part) {
        if (part == 0 || part == 1) {
            Optional<String> apiKey = this.getValue(installation, CONFIG_MC_API_KEY);
            if (apiKey.isPresent() && StringUtils.hasText(apiKey.get())) {
                String[] meta = apiKey.get().split("-");
                if (meta.length == 2 && StringUtils.hasText(meta[part])) {
                    return Optional.of(meta[part]);
                }
            }
        }
        return Optional.empty();
    }
}
