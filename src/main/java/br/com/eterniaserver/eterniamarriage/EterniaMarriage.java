package br.com.eterniaserver.eterniamarriage;

import br.com.eterniaserver.eternialib.EterniaLib;
import br.com.eterniaserver.eterniamarriage.configurations.ConfigsCfg;
import br.com.eterniaserver.eterniamarriage.configurations.MessagesCfg;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.MarryId;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.PlayerMarry;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.PlayerMarryPropose;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.PlayerTeleport;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.Religion;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.ReligionInvite;
import br.com.eterniaserver.eterniamarriage.core.enums.Doubles;
import br.com.eterniaserver.eterniamarriage.core.enums.Messages;
import br.com.eterniaserver.eterniamarriage.core.enums.Strings;
import br.com.eterniaserver.eterniamarriage.core.*;

import br.com.eterniaserver.eterniamarriage.handlers.McMMOHandler;
import br.com.eterniaserver.eterniamarriage.handlers.PlayerHandler;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EterniaMarriage extends JavaPlugin {

    private final String[] strings = new String[Strings.values().length];
    private final String[] messages = new String[Messages.values().length];
    private final double[] doubles = new double[Doubles.values().length];

    public Economy economy;

    public final Map<Integer, MarryId> marrieds = new HashMap<>();
    public final Map<UUID, PlayerMarry> marriedUsers = new HashMap<>();
    public final Map<UUID, Religion> religions = new HashMap<>();
    public final Map<UUID, ReligionInvite> invited = new HashMap<>();
    public final Map<UUID, Long> userKiss = new HashMap<>();
    public final Map<Integer, Boolean> marryOnline = new HashMap<>();
    public final Map<Player, PlayerTeleport> teleports = new HashMap<>();
    public final Map<String, Integer> proposesId = new HashMap<>();
    public final Map<Integer, PlayerMarryPropose> marryProposes = new HashMap<>();

    @Override
    public void onEnable() {
        loadConfigurations();
        new Metrics(this, 10394);
        new Manager(this);
    }

    private void loadConfigurations() {
        final ConfigsCfg configsCfg = new ConfigsCfg(this, strings, doubles);
        final MessagesCfg messagesCfg = new MessagesCfg(messages);

        EterniaLib.addReloadableConfiguration("eterniamarriage", "configs", configsCfg);
        EterniaLib.addReloadableConfiguration("eterniamarriage", "messages", messagesCfg);

        configsCfg.executeConfig();
        configsCfg.executeCritical();
        messagesCfg.executeConfig();
    }

    public String getString(Strings enumValue) {
        return strings[enumValue.ordinal()];
    }

    public double getDouble(Doubles enumValue) {
        return doubles[enumValue.ordinal()];
    }

    public void sendMessage(final CommandSender sender, final Messages entry, final boolean prefix, final String... args) {
        sender.sendMessage(getMessage(entry, prefix, args));
    }

    public void sendMessage(final CommandSender sender, final Messages entry, final String... args) {
        sender.sendMessage(getMessage(entry, true, args));
    }

    public String getMessage(final Messages entry, final boolean prefix, final String... args) {
        String message = messages[entry.ordinal()];

        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", args[i]);
        }

        if (prefix) {
            return getString(Strings.SERVER_PREFIX) + message;
        }

        return message;
    }

}
