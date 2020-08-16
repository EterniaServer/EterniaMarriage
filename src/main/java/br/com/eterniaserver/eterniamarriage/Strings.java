package br.com.eterniaserver.eterniamarriage;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class Strings {

    private Strings() {
        throw new IllegalStateException("Utility class");
    }

    public static final String UUID = "uuid";
    public static final String MARRY_UUID = "marry_uuid";
    public static final String MARRY_ID = "marry_id";
    public static final String MARRY_NAME = "marry_name";
    public static final String MARRY_DISPLAY = "marry_display";
    public static final String BALANCE = "balance";
    public static final String LOC = "location";
    public static final String HOURS = "hours";
    public static final String TIME = "time";
    public static final String LAST = "last";

    public static void reloadConfig(FileConfiguration msgConfig) {
        M_SERVER_PREFIX = getColor(msgConfig.getString("server.prefix"));
        M_SERVER_RELOAD = putPrefix(msgConfig, "server.reload");
        M_SERVER_YOUR = putPrefix(msgConfig, "server.yourself");
        M_SERVER_LOAD = putPrefix(msgConfig, "server.loaded");
        M_SERVER_TIMING = putPrefix(msgConfig, "server.timing");
        M_SERVER_MOVE = putPrefix(msgConfig, "server.move");
        M_NO_MONEY = putPrefix(msgConfig, "server.no-money");
        M_NO_BAL = putPrefix(msgConfig, "server.no-bal");
        M_BALANCE_NO = putPrefix(msgConfig, "server.no-balance");
        M_MARRY_ALREADY = putPrefix(msgConfig, "marry.already-married");
        M_MARRY_ALREADY_SENT = putPrefix(msgConfig, "marry.already-sent");
        M_MARRY_ADVICE = putPrefix(msgConfig, "marry.advice");
        M_MARRY_NO = putPrefix(msgConfig, "marry.no-marry");
        M_MARRY_SUCESS = putPrefix(msgConfig, "marry.sucess");
        M_MARRY_ACCEPT = putPrefix(msgConfig, "marry.accept");
        M_MARRY_PROPOSAL = putPrefix(msgConfig, "marry.no-proposal");
        M_MARRY_DENY = putPrefix(msgConfig, "marry.deny");
        M_MARRY_SENT = putPrefix(msgConfig, "marry.send-proposal");
        M_COMMANDS_DEPOSIT = putPrefix(msgConfig, "commands.deposit");
        M_COMMANDS_NO_MARRY = putPrefix(msgConfig, "commands.no-marry");
        M_COMMANDS_OFFLINE = putPrefix(msgConfig, "commands.offline");
        M_COMMANDS_NO_MONEY = putPrefix(msgConfig, "commands.no-money");
        M_COMMANDS_DONE = putPrefix(msgConfig, "commands.home");
        M_COMMANDS_NO_HOME = putPrefix(msgConfig, "commands.no-home");
        M_COMMANDS_HOME_SAVE = putPrefix(msgConfig, "commands.home-save");
        M_COMMANDS_BALANCE = putPrefix(msgConfig, "commands.balance");
    }

    public static String M_SERVER_PREFIX;
    public static String M_SERVER_RELOAD;
    public static String M_SERVER_YOUR;
    public static String M_SERVER_LOAD;
    public static String M_SERVER_TIMING;
    public static String M_SERVER_MOVE;
    public static String M_NO_MONEY;
    public static String M_NO_BAL;

    public static String M_BALANCE_NO;

    public static String M_MARRY_ALREADY;
    public static String M_MARRY_ALREADY_SENT;
    public static String M_MARRY_ADVICE;
    public static String M_MARRY_NO;
    public static String M_MARRY_SUCESS;
    public static String M_MARRY_ACCEPT;
    public static String M_MARRY_PROPOSAL;
    public static String M_MARRY_DENY;
    public static String M_MARRY_SENT;

    public static String M_COMMANDS_DEPOSIT;
    public static String M_COMMANDS_NO_MARRY;
    public static String M_COMMANDS_OFFLINE;
    public static String M_COMMANDS_NO_MONEY;
    public static String M_COMMANDS_DONE;
    public static String M_COMMANDS_NO_HOME;
    public static String M_COMMANDS_HOME_SAVE;
    public static String M_COMMANDS_BALANCE;

    private static String putPrefix(FileConfiguration msg, String path) {
        String message = msg.getString(path);
        if (message == null) message = "&7Erro&8, &7texto &3" + path + "&7não encontrado&8.";
        return M_SERVER_PREFIX + getColor(message);
    }

    public static String getColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}
