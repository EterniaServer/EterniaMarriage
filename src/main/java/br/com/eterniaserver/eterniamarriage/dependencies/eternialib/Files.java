package br.com.eterniaserver.eterniamarriage.dependencies.eternialib;

import br.com.eterniaserver.eternialib.EFiles;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;
import java.util.Locale;

public class Files {

    private final EterniaMarriage plugin;

    public Files(EterniaMarriage plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {

        try {
            EterniaMarriage.serverConfig.load(EFiles.fileLoad(plugin, "config.yml"));

            EFiles.fileLoad(plugin, "acf_messages.yml");
            plugin.getManager().getLocales().loadYamlLanguageFile("acf_messages.yml", Locale.ENGLISH);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

    }

    public void loadMessages() {

        try {
            EterniaMarriage.msgConfig.load(EFiles.fileLoad(plugin, "messages.yml"));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

    }

    public void loadDatabase() {

        new Table();

    }

}
