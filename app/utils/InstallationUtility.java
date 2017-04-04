package utils;

import models.Installation;
import models.Preference;

import javax.inject.Singleton;
import java.util.Optional;

import static utils.Constants.CONFIG_MC_API_KEY;
import static utils.Constants.CONFIG_MC_LIST_ID;

/**
 * @author rishabh
 */
@Singleton
public class InstallationUtility {


    public Optional<String> getConfiguredListId(Installation installation) {
        Optional<String> value = Optional.empty();
        for (Preference preference : installation.getPreferences()) {
            if (preference.getKey().equals(CONFIG_MC_LIST_ID)) {
                String label = preference.getValues().get(0).getId();
                value = Optional.ofNullable(label);
            }
        }
        return value;
    }

    public Optional<String> getDataCentre(Installation installation) {
        Optional<String> value = Optional.empty();
        for (Preference preference : installation.getPreferences()) {
            if (preference.getKey().equals(CONFIG_MC_API_KEY)) {
                String label = (String) preference.getValues().get(0).getLabel();
                String[] arr = label.split("-");
                value = Optional.ofNullable(arr[1]);
            }
        }
        return value;
    }

    public Optional<String> getApiKey(Installation installation) {
        Optional<String> value = Optional.empty();
        for (Preference preference : installation.getPreferences()) {
            if (preference.getKey().equals(CONFIG_MC_API_KEY)) {
                String label = (String) preference.getValues().get(0).getLabel();
                String[] arr = label.split("-");
                value = Optional.ofNullable(arr[0]);
            }
        }
        return value;
    }

}
