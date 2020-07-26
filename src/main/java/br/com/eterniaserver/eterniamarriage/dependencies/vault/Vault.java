package br.com.eterniaserver.eterniamarriage.dependencies.vault;

import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault {

    public Vault() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        EterniaMarriage.econ = rsp.getProvider();
    }


}
