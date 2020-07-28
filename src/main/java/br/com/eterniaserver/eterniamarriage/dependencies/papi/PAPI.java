package br.com.eterniaserver.eterniamarriage.dependencies.papi;

import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.Strings;
import me.clip.placeholderapi.PlaceholderAPI;

public class PAPI {
    public PAPI(EterniaMarriage plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            plugin.getEFiles().sendConsole(Strings.M_NO_PAPI);
        } else {
            PlaceholderAPI.registerPlaceholderHook("eterniamarriage", plugin.getPlaceHolders());
        }
    }
}
