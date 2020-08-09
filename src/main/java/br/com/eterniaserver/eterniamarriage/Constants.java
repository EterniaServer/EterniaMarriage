package br.com.eterniaserver.eterniamarriage;

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

    public static final String TABLE_MARRY = EterniaMarriage.serverConfig.getString("sql.table-marry");
    public static final String TABLE_BANK = EterniaMarriage.serverConfig.getString("sql.table-bank");

    public static String getQueryCreateTable(final String table, final String values) {
        return "CREATE TABLE IF NOT EXISTS " + table + " " + values + ";";
    }

    public static String getQuerySelectAll(final String table)   {
        return "SELECT * FROM " + table + ";";
    }

    public static String getQueryDelete(final String table, final String type, final String value) {
        return "DELETE FROM " + table + " WHERE " + type + "='" + value + "';";
    }

    public static String getQueryUpdate(final String table, final String type, final Object value, final String type2, final Object value2) {
        return "UPDATE " + table + " SET " + type + "='" + value + "' WHERE " + type2 + "='" + value2 + "';";
    }

    public static String getQueryInsert(final String table, final String type, final Object value) {
        return "INSERT INTO " + table + " " + type + " VALUES " + value + ";";
    }

}
