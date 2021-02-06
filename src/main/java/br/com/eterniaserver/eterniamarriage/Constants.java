package br.com.eterniaserver.eterniamarriage;

import java.io.File;

import org.bukkit.command.CommandSender;

import br.com.eterniaserver.eterniamarriage.enums.Messages;
import br.com.eterniaserver.eterniamarriage.enums.Strings;

public class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String PLAYER = "%player_displayname%";
    public static final String TARGET = "%target_displayname%";
    public static final String MODULE = "%module%";
    public static final String AMOUNT = "%amount%";
    public static final String MONEY = "%money%";
    public static final String COOLDOWN = "%cooldown%";

    public static final String DATA_LAYER_FOLDER_PATH = "plugins" + File.separator + "EterniaMarriage";
    public static final String CONFIG_FILE_PATH = DATA_LAYER_FOLDER_PATH + File.separator + "config.yml";
    public static final String MESSAGES_FILE_PATH = DATA_LAYER_FOLDER_PATH + File.separator + "messages.yml";

    protected void sendMessage(CommandSender sender, Messages messagesId, String... args) {
        sendMessage(sender, messagesId, true, args);
    }

    protected static void sendMessage(CommandSender sender, Messages messagesId, boolean prefix, String... args) {
        sender.sendMessage(EterniaMarriage.getMessage(messagesId, prefix, args));
    }

    protected static String getMessage(Messages messagesId, boolean prefix, String[] messages, String... args) {
        String message = messages[messagesId.ordinal()];

        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", args[i]);
        }

        if (prefix) {
            return EterniaMarriage.getString(Strings.SERVER_PREFIX) + message;
        }

        return message;
    }

}
