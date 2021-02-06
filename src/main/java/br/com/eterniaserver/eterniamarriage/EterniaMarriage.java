package br.com.eterniaserver.eterniamarriage;

import br.com.eterniaserver.eternialib.CommandManager;
import br.com.eterniaserver.eterniamarriage.dependencies.ConfigsCfg;
import br.com.eterniaserver.eterniamarriage.dependencies.PlaceHolders;
import br.com.eterniaserver.eterniamarriage.dependencies.eternialib.Tables;
import br.com.eterniaserver.eterniamarriage.enums.Doubles;
import br.com.eterniaserver.eterniamarriage.enums.Messages;
import br.com.eterniaserver.eterniamarriage.enums.Strings;
import br.com.eterniaserver.eterniamarriage.core.*;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EterniaMarriage extends JavaPlugin {

    private static Economy econ;

    private static final String[] stringsConfig = new String[Strings.values().length];
    private static final Double[] doublesConfig = new Double[Doubles.values().length];
    private static final String[] messagesConfig = new String[Messages.values().length];

    @Override
    public void onEnable() {

        loadConfigurations();
        vault();

        new Tables();

        CommandManager.registerCommand(new MarryCommand());
        CommandManager.registerCommand(new ReligionCommand());

        this.getServer().getPluginManager().registerEvents(new Events(), this);
        this.getServer().getPluginManager().registerEvents(new OnMcMMOPlayerXpGain(), this);

        new Checks().runTaskTimer(this, 0L, 20L);

        new PlaceHolders().register();

    }

    public static String getString(Strings enumValue) {
        return stringsConfig[enumValue.ordinal()];
    }

    public static double getDouble(Doubles enumValue) {
        return doublesConfig[enumValue.ordinal()];
    }

    public static void loadConfigurations() {
        new ConfigsCfg(stringsConfig, doublesConfig, messagesConfig);
    }

    public static String getMessage(Messages messagesId, boolean prefix, String... args) {
        return Constants.getMessage(messagesId, prefix, messagesConfig, args);
    }

    public static void sendMessage(CommandSender sender, Messages messagesId, String... args) {
        Constants.sendMessage(sender, messagesId, true, args);
    }

    public static void sendMessage(CommandSender sender, Messages messagesId, boolean prefix, String... args) {
        Constants.sendMessage(sender, messagesId, prefix, args);
    }

    public static Economy getEcon() {
        return econ;
    }

    private static void vault() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                econ = rsp.getProvider();
            }
        }
    }

}
