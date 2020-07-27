package br.com.eterniaserver.eterniamarriage;

import br.com.eterniaserver.eternialib.EFiles;
import br.com.eterniaserver.eterniamarriage.dependencies.eternialib.Files;
import br.com.eterniaserver.eterniamarriage.generic.*;
import co.aikar.commands.PaperCommandManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class EterniaMarriage extends JavaPlugin {

    private PaperCommandManager manager;
    private EFiles messages;
    private Files files;

    public static final FileConfiguration serverConfig = new YamlConfiguration();
    public static final FileConfiguration msgConfig = new YamlConfiguration();

    public static Economy econ = null;

    @Override
    public void onEnable() {

        manager = new PaperCommandManager(this);
        files = new Files(this);

        files.loadConfigs();
        files.loadMessages();
        files.loadDatabase();

        messages = new EFiles(msgConfig);

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

}
