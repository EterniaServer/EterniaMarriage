package br.com.eterniaserver.eterniamarriage.configurations;

import br.com.eterniaserver.eternialib.EterniaLib;
import br.com.eterniaserver.eternialib.SQL;
import br.com.eterniaserver.eternialib.core.enums.ConfigurationCategory;
import br.com.eterniaserver.eternialib.core.interfaces.ReloadableConfiguration;
import br.com.eterniaserver.eternialib.core.queries.CreateTable;
import br.com.eterniaserver.eternialib.core.queries.Select;
import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.Religion;
import br.com.eterniaserver.eterniamarriage.core.enums.Doubles;
import br.com.eterniaserver.eterniamarriage.core.enums.Messages;
import br.com.eterniaserver.eterniamarriage.core.enums.Strings;

import br.com.eterniaserver.eterniamarriage.core.baseobjects.MarryId;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.PlayerMarry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfigsCfg implements ReloadableConfiguration {

    private final EterniaMarriage plugin;

    private final String[] strings;
    private final double[] doubles;

    public ConfigsCfg(final EterniaMarriage plugin, final String[] strings, final double[] doubles) {
        this.plugin = plugin;
        this.strings = strings;
        this.doubles = doubles;
    }

    @Override
    public ConfigurationCategory category() {
        return ConfigurationCategory.WARNING_ADVICE;
    }

    @Override
    public void executeConfig() {
        // Load the configurations
        final FileConfiguration config = YamlConfiguration.loadConfiguration(new File(Constants.CONFIG_FILE_PATH));

        strings[Strings.TABLE_MARRY.ordinal()] = config.getString("sql.table-marry", "em_players");
        strings[Strings.TABLE_BANK.ordinal()] = config.getString("sql.table-bank", "em_ids");
        strings[Strings.TABLE_RELIGION.ordinal()] = config.getString("sql.table-religion", "em_religion");
        strings[Strings.SERVER_PREFIX.ordinal()] = config.getString("server.prefix", "$8[$aE$9M$8]$7 ").replace('$', (char) 0x00A7);
        strings[Strings.PLACEHOLDER_CLOSE_TO_PARTNER.ordinal()] = config.getString("placeholders.isclose", "$3Perto").replace('$', (char) 0x00A7);
        strings[Strings.PLACEHOLDER_DEFAULT_RELIGION.ordinal()] = config.getString("placeholders.default-religion", "♰").replace('$', (char) 0x00A7);
        strings[Strings.PLACEHOLDER_PARTNER.ordinal()] = config.getString("placeholders.partner", "❤").replace('$', (char) 0x00A7);

        doubles[Doubles.MARRY_COST.ordinal()] = config.getDouble("money.marry", 200000.0);
        doubles[Doubles.SETHOME_COST.ordinal()] = config.getDouble("money.sethome", 2000.0);
        doubles[Doubles.SERVER_COOLDOWN.ordinal()] = config.getDouble("server.cooldown", 4.0);
        doubles[Doubles.RELIGION_MAX_NAME.ordinal()] = config.getDouble("religion.max-name", 12.0);
        doubles[Doubles.RELIGION_MAX_PREFIX.ordinal()] = config.getDouble("religion.max-prefix", 1.0);

        // Save the configurations
        final FileConfiguration outConfig = new YamlConfiguration();

        outConfig.set("sql.table-marry", strings[Strings.TABLE_MARRY.ordinal()]);
        outConfig.set("sql.table-bank", strings[Strings.TABLE_BANK.ordinal()]);
        outConfig.set("sql.table-religion", strings[Strings.TABLE_RELIGION.ordinal()]);
        outConfig.set("server.prefix", strings[Strings.SERVER_PREFIX.ordinal()]);
        outConfig.set("placeholders.isclose", strings[Strings.PLACEHOLDER_CLOSE_TO_PARTNER.ordinal()]);
        outConfig.set("placeholders.default-religion", strings[Strings.PLACEHOLDER_DEFAULT_RELIGION.ordinal()]);
        outConfig.set("placeholders.partner", strings[Strings.PLACEHOLDER_PARTNER.ordinal()]);

        outConfig.set("money.marry", doubles[Doubles.MARRY_COST.ordinal()]);
        outConfig.set("money.sethome", doubles[Doubles.SETHOME_COST.ordinal()]);
        outConfig.set("server.cooldown", doubles[Doubles.SERVER_COOLDOWN.ordinal()]);
        outConfig.set("religion.max-name", doubles[Doubles.RELIGION_MAX_NAME.ordinal()]);
        outConfig.set("religion.max-prefix", doubles[Doubles.RELIGION_MAX_PREFIX.ordinal()]);

        outConfig.options().header("Caso precise de ajuda acesse https://github.com/EterniaServer/EterniaMarriage/wiki");

        try {
            outConfig.save(Constants.CONFIG_FILE_PATH);
        } catch (IOException exception) { }
    }

    @Override
    public void executeCritical() {

        final String marryId = "marry_id INT(8)";

        CreateTable createTable = new CreateTable(strings[Strings.TABLE_RELIGION.ordinal()]);
        createTable.columns.set("uuid VARCHAR(36)", "religion_name VARCHAR(36)", "religion_prefix VARCHAR(36)");
        SQL.execute(createTable);

        createTable = new CreateTable(strings[Strings.TABLE_MARRY.ordinal()]);
        createTable.columns.set("uuid VARCHAR(36)", "marry_uuid VARCHAR(36)", "marry_name VARCHAR(16)", "marry_display VARCHAR(16)", marryId);
        SQL.execute(createTable);

        if (EterniaLib.getMySQL()) {
            createTable = new CreateTable(strings[Strings.TABLE_BANK.ordinal()]);
            createTable.columns.set(marryId, "balance DOUBLE(22,4)", "hours INTEGER(4)", "location VARCHAR(64)", "time BIGINT(20)", "last BIGINT(20)");
            SQL.execute(createTable);
        } else {
            createTable = new CreateTable(strings[Strings.TABLE_BANK.ordinal()]);
            createTable.columns.set(marryId, "balance DOUBLE(22)", "hours INTEGER(4)", "location VARCHAR(64)", "time INTEGER", "last INTEGER");
            SQL.execute(createTable);
        }

        plugin.marriedUsers.clear();
        plugin.marrieds.clear();

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(new Select(plugin.getString(Strings.TABLE_MARRY)).queryString()); ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                final UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                final UUID marryUUID = UUID.fromString(resultSet.getString("marry_uuid"));
                plugin.marriedUsers.put(uuid, new PlayerMarry(
                        uuid,
                        marryUUID,
                        resultSet.getString("marry_name"),
                        resultSet.getString("marry_display"),
                        resultSet.getInt("marry_id")
                ));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        EterniaLib.report(plugin.getMessage(Messages.SERVER_LOADED, true, "Married Users", String.valueOf(plugin.marriedUsers.size())));

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(new Select(plugin.getString(Strings.TABLE_BANK)).queryString()); ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                final int marryIdQ = resultSet.getInt("marry_id");
                final String[] split = resultSet.getString("location").split(":");
                final Location loc = new Location(Bukkit.getWorld(split[0]),
                        Double.parseDouble(split[1]),
                        (Double.parseDouble(split[2]) + 1),
                        Double.parseDouble(split[3]),
                        Float.parseFloat(split[4]),
                        Float.parseFloat(split[5]));

                plugin.marrieds.put(marryIdQ, new MarryId(
                        marryIdQ,
                        resultSet.getDouble("balance"),
                        resultSet.getInt("hours"),
                        loc,
                        resultSet.getLong("time"),
                        resultSet.getLong("last")
                ));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        EterniaLib.report(plugin.getMessage(Messages.SERVER_LOADED, true, "Marry Accounts", String.valueOf(plugin.marrieds.size())));

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(new Select(plugin.getString(Strings.TABLE_RELIGION)).queryString()); ResultSet resultSet = preparedStatement.executeQuery()) {
            Map<String, Religion> religionCache = new HashMap<>();
            while (resultSet.next()) {
                final String religionName = resultSet.getString("religion_name");
                if (religionCache.containsKey(religionName)) {
                    plugin.religions.put(UUID.fromString(resultSet.getString("uuid")), religionCache.get(religionName));
                } else {
                    br.com.eterniaserver.eterniamarriage.core.baseobjects.Religion religion = new br.com.eterniaserver.eterniamarriage.core.baseobjects.Religion(religionName, resultSet.getString("religion_prefix"));
                    religionCache.put(religionName, religion);
                    plugin.religions.put(UUID.fromString(resultSet.getString("uuid")), religion);
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        EterniaLib.report(plugin.getMessage(Messages.SERVER_LOADED, true, "Religions", String.valueOf(plugin.religions.size())));


    }
}
