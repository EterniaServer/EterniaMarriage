package br.com.eterniaserver.eterniamarriage;

import br.com.eterniaserver.eternialib.EFiles;
import br.com.eterniaserver.eternialib.EterniaLib;
import br.com.eterniaserver.eterniamarriage.dependencies.eternialib.Files;
import br.com.eterniaserver.eterniamarriage.generic.*;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EterniaMarriage extends JavaPlugin {

    private EFiles messages;
    private Files files;

    public static final FileConfiguration serverConfig = new YamlConfiguration();
    public static final FileConfiguration msgConfig = new YamlConfiguration();

    private Economy econ;

    @Override
    @SuppressWarnings("deprecation")
    public void onEnable() {

        files = new Files(this);

        files.loadConfigs();
        files.loadMessages();
        files.loadDatabase();

        messages = new EFiles(msgConfig);
        new PlaceHolders().register();

        vault();

        EterniaLib.getManager().registerCommand(new Commands(this));

        this.getServer().getPluginManager().registerEvents(new OnMcMMOPlayerXpGain(), this);
        this.getServer().getScheduler().runTaskTimer(this, new Checks(this), 0L, 100L);

    }

    @Override
    public void onDisable() {
        new MarryTime().saveTime();
    }

    public Files getFiles() {
        return files;
    }

    public EFiles getEFiles() {
        return messages;
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
