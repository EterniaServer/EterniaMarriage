package br.com.eterniaserver.eterniamarriage;

import java.io.File;

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

}
