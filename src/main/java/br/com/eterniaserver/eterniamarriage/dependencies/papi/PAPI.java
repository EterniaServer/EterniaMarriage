package br.com.eterniaserver.eterniamarriage.dependencies.papi;

import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import me.clip.placeholderapi.PlaceholderAPI;

public class PAPI {
    public PAPI(EterniaMarriage plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            plugin.getEFiles().sendConsole("server.no-papi");
        } else {
            PlaceholderAPI.registerPlaceholderHook("eterniamarriage", plugin.getPlaceHolders());
        }
    }
}
