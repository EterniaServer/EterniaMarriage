package br.com.eterniaserver.eterniamarriage.dependencies.eternialib;

import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.Strings;

import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;

public class Files {

    private final EterniaMarriage plugin;

    public Files(EterniaMarriage plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {

        final String messages = "config.yml";

        final File file = new File(plugin.getDataFolder(), messages);
        if (!file.exists()) plugin.saveResource(messages, false);

        try {
            EterniaMarriage.serverConfig.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

    }

    public void loadMessages() {

        final String messages = "messages.yml";

        final File file = new File(plugin.getDataFolder(), messages);
        if (!file.exists()) plugin.saveResource(messages, false);

        try {
            EterniaMarriage.msgConfig.load(file);
            Strings.reloadConfig(EterniaMarriage.msgConfig);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

    }

    public void loadDatabase() {

        Constants.reloadConfig();
        new Table();

    }

}
