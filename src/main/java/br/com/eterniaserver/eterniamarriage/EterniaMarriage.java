package br.com.eterniaserver.eterniamarriage;

import br.com.eterniaserver.eternialib.EterniaLib;
import br.com.eterniaserver.eterniamarriage.dependencies.eternialib.Files;
import br.com.eterniaserver.eterniamarriage.generics.*;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EterniaMarriage extends JavaPlugin {

    private Files files;

    public static final FileConfiguration serverConfig = new YamlConfiguration();
    public static final FileConfiguration msgConfig = new YamlConfiguration();

    private Economy econ;

    @Override
    public void onEnable() {

        files = new Files(this);

        files.loadConfigs();
        files.loadMessages();
        files.loadDatabase();

        vault();

        EterniaLib.getManager().registerCommand(new Commands(this));

        this.getServer().getPluginManager().registerEvents(new Events(), this);
        this.getServer().getPluginManager().registerEvents(new OnMcMMOPlayerXpGain(), this);
        this.getServer().getScheduler().runTaskTimer(this, new Checks(), 0L, 20L);

        new PlaceHolders().register();

    }

    public Files getFiles() {
        return files;
    }

    public Economy getEcon() {
        return econ;
    }

    public void vault() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                econ = rsp.getProvider();
            }
        }
    }

}
