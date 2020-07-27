package br.com.eterniaserver.eterniamarriage;

import br.com.eterniaserver.eternialib.EFiles;
import br.com.eterniaserver.eterniamarriage.dependencies.eternialib.Files;
import br.com.eterniaserver.eterniamarriage.dependencies.papi.PAPI;
import br.com.eterniaserver.eterniamarriage.generic.*;

import co.aikar.commands.PaperCommandManager;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;

public class EterniaMarriage extends JavaPlugin {

    public final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final PlaceHolders placeHolders = new PlaceHolders();

    private PaperCommandManager manager;
    private EFiles messages;
    private Files files;

    public static final FileConfiguration serverConfig = new YamlConfiguration();
    public static final FileConfiguration msgConfig = new YamlConfiguration();

    private Economy econ;

    @Override
    public void onEnable() {

        manager = new PaperCommandManager(this);
        files = new Files(this);

        files.loadConfigs();
        files.loadMessages();
        files.loadDatabase();

        messages = new EFiles(msgConfig);

        placeholderAPIHook();
        vault();

        manager.registerCommand(new Commands(this));

        this.getServer().getPluginManager().registerEvents(new OnMcMMOPlayerXpGain(), this);
        this.getServer().getScheduler().runTaskTimer(this, new Checks(this), 0L, 100L);

    }

    @Override
    public void onDisable() {
        new MarryTime().saveTime();
    }

    public PaperCommandManager getManager() {
        return manager;
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

    private void placeholderAPIHook() {
        new PAPI(this);
    }

    public PlaceHolders getPlaceHolders() {
        return placeHolders;
    }

    public void vault() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            econ = rsp.getProvider();
        }
    }

}
