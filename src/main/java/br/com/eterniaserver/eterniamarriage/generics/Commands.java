package br.com.eterniaserver.eterniamarriage.generics;

import br.com.eterniaserver.acf.BaseCommand;
import br.com.eterniaserver.acf.annotation.*;
import br.com.eterniaserver.acf.bukkit.contexts.OnlinePlayer;
import br.com.eterniaserver.eternialib.EQueries;
import br.com.eterniaserver.eternialib.EterniaLib;
import br.com.eterniaserver.eternialib.UUIDFetcher;
import br.com.eterniaserver.eternialib.sql.Connections;
import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.Strings;
import br.com.eterniaserver.eterniamarriage.objects.MarryId;
import br.com.eterniaserver.eterniamarriage.objects.PlayerMarry;
import br.com.eterniaserver.eterniamarriage.objects.PlayerMarryPropose;
import br.com.eterniaserver.eterniamarriage.objects.PlayerTeleport;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandAlias("marry")
@CommandPermission("eternia.marry")
public class Commands extends BaseCommand {

    private final Economy economy;
    private final EterniaMarriage plugin;
    private final ItemStack air = new ItemStack(Material.AIR);
    private final Location error;

    public Commands(EterniaMarriage plugin) {
        error = new Location(Bukkit.getWorld("world"), 666, 666, 666, 666, 666);
        this.plugin = plugin;
        this.economy = plugin.getEcon();

        if (EterniaLib.getMySQL()) {
            EterniaLib.getConnections().executeSQLQuery(connection -> {
                PreparedStatement getHashMap = connection.prepareStatement(Constants.getQuerySelectAll(Constants.TABLE_MARRY));
                ResultSet resultSet = getHashMap.executeQuery();
                while (resultSet.next()) {
                    final UUID uuid = UUID.fromString(resultSet.getString(Strings.UUID));
                    final UUID marryUUID = UUID.fromString(resultSet.getString(Strings.MARRY_UUID));
                    Vars.marriedUsers.put(uuid, new PlayerMarry(
                            uuid,
                            marryUUID,
                            resultSet.getString(Strings.MARRY_NAME),
                            resultSet.getString(Strings.MARRY_DISPLAY),
                            resultSet.getInt(Strings.MARRY_ID)
                    ));
                }

                getHashMap = connection.prepareStatement(Constants.getQuerySelectAll(Constants.TABLE_BANK));
                resultSet = getHashMap.executeQuery();
                while (resultSet.next()) {
                    final int marryId = resultSet.getInt(Strings.MARRY_ID);
                    final String[] split = resultSet.getString(Strings.LOC).split(":");
                    final Location loc = new Location(Bukkit.getWorld(split[0]),
                            Double.parseDouble(split[1]),
                            (Double.parseDouble(split[2]) + 1),
                            Double.parseDouble(split[3]),
                            Float.parseFloat(split[4]),
                            Float.parseFloat(split[5]));

                    Vars.marrieds.put(marryId, new MarryId(
                            marryId,
                            resultSet.getDouble(Strings.BALANCE),
                            resultSet.getInt(Strings.HOURS),
                            loc,
                            resultSet.getLong(Strings.TIME),
                            resultSet.getLong(Strings.LAST)
                    ));
                }
                getHashMap.close();
                resultSet.close();
            });
        } else {
            try (PreparedStatement getHashMap = Connections.getSQLite().prepareStatement(Constants.getQuerySelectAll(Constants.TABLE_MARRY)); ResultSet resultSet = getHashMap.executeQuery()) {
                while (resultSet.next()) {
                    final UUID uuid = UUID.fromString(resultSet.getString(Strings.UUID));
                    Vars.marriedUsers.put(uuid, new PlayerMarry(
                            uuid,
                            UUID.fromString(resultSet.getString(Strings.MARRY_UUID)),
                            resultSet.getString(Strings.MARRY_NAME),
                            resultSet.getString(Strings.MARRY_DISPLAY),
                            resultSet.getInt(Strings.MARRY_ID)
                    ));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            try (PreparedStatement getHashMap = Connections.getSQLite().prepareStatement(Constants.getQuerySelectAll(Constants.TABLE_BANK)); ResultSet resultSet = getHashMap.executeQuery()) {
                while (resultSet.next()) {
                    final int marryId = resultSet.getInt(Strings.MARRY_ID);
                    final String[] split = resultSet.getString(Strings.LOC).split(":");
                    final Location loc = new Location(Bukkit.getWorld(split[0]),
                            Double.parseDouble(split[1]),
                            (Double.parseDouble(split[2]) + 1),
                            Double.parseDouble(split[3]),
                            Float.parseFloat(split[4]),
                            Float.parseFloat(split[5]));
                    Vars.marrieds.put(marryId, new MarryId(
                            marryId,
                            resultSet.getDouble(Strings.BALANCE),
                            resultSet.getInt(Strings.HOURS),
                            loc,
                            resultSet.getLong(Strings.TIME),
                            resultSet.getLong(Strings.LAST)
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        sendConsole(Strings.M_SERVER_LOAD.replace(Constants.MODULE, "Married Users").replace(Constants.AMOUNT, String.valueOf(Vars.marriedUsers.size())));
        sendConsole(Strings.M_SERVER_LOAD.replace(Constants.MODULE, "Marry Accounts").replace(Constants.AMOUNT, String.valueOf(Vars.marrieds.size())));
    }

    @Default
    @Syntax("<jogador(a)> <jogador(a)>")
    @CommandCompletion("@players @players")
    @CommandPermission("eternia.priest")
    public void onMarry(Player player, OnlinePlayer marryOne, OnlinePlayer marryTwo) {
        final double money = EterniaMarriage.serverConfig.getDouble("money.marry");

        if (!economy.has(player, money)) {
            player.sendMessage(Strings.M_BALANCE_NO.replace(Constants.MONEY, String.valueOf(money)));
            return;
        }

        final Player wife = marryOne.getPlayer();
        final Player husband = marryTwo.getPlayer();
        final String wifeName = wife.getName();
        final String husbandName = husband.getName();

        if (wifeName.equals(husbandName)) {
            player.sendMessage(Strings.M_SERVER_YOUR);
            return;
        }

        final UUID wifeUUID = UUIDFetcher.getUUIDOf(wifeName);
        final UUID husbandUUID = UUIDFetcher.getUUIDOf(husbandName);
        if (APIMarry.isMarried(wifeUUID) || APIMarry.isMarried(husbandUUID)) {
            player.sendMessage(Strings.M_MARRY_ALREADY);
            return;
        }

        if (Vars.proposesId.containsKey(wifeName) || Vars.proposesId.containsKey(husbandName)) {
            player.sendMessage(Strings.M_MARRY_ALREADY_SENT);
            return;
        }

        Bukkit.broadcastMessage(Strings.M_MARRY_ADVICE.replace(Constants.PLAYER, wife.getDisplayName()).replace(Constants.TARGET, husband.getDisplayName()));
        wife.sendMessage(Strings.M_MARRY_SENT.replace(Constants.PLAYER, wife.getDisplayName()).replace(Constants.TARGET, husband.getDisplayName()));
        husband.sendMessage(Strings.M_MARRY_SENT.replace(Constants.PLAYER, husband.getDisplayName()).replace(Constants.TARGET, wife.getDisplayName()));

        economy.withdrawPlayer(player, money);
        Vars.marryProposes.put(Vars.marrieds.size() + 1, new PlayerMarryPropose(wifeUUID, husbandUUID));
        Vars.proposesId.put(wifeName, Vars.marrieds.size() + 1);
        Vars.proposesId.put(husbandName, Vars.marrieds.size() + 1);
    }

    @Subcommand("reload")
    @CommandPermission("eternia.reload")
    public void onReload(Player player) {
        plugin.getFiles().loadConfigs();
        plugin.getFiles().loadDatabase();
        plugin.getFiles().loadMessages();
        player.sendMessage(Strings.M_SERVER_RELOAD);
    }

    @Subcommand("divorce")
    @Syntax("<jogador(a)> <jogador(a)>")
    @CommandCompletion("@players @players")
    @CommandPermission("eternia.priest")
    public void onDivorce(Player player, String wifeName, String husbandName) {
        CompletableFuture.runAsync(() -> {
            final UUID wifeUUID = UUIDFetcher.getUUIDOf(wifeName);
            final UUID husbandUUID = UUIDFetcher.getUUIDOf(husbandName);
            if (APIMarry.isMarried(wifeUUID)) {
                if (APIMarry.getPartnerUUID(wifeUUID).equals(husbandUUID)) {
                    marryDeny(wifeUUID, husbandUUID);
                    return;
                }
            }
            player.sendMessage(Strings.M_MARRY_NO);
        });
    }

    @Subcommand("accept")
    public void onAccept(Player player) {
        final String wifeName = player.getName();
        if (Vars.proposesId.containsKey(wifeName)) {
            final int id = Vars.proposesId.get(wifeName);
            final PlayerMarryPropose playerMarryPropose = Vars.marryProposes.get(id);

            playerMarryPropose.setMarryAccept();

            if (playerMarryPropose.getMarryAccept()) {
                Bukkit.broadcastMessage(Strings.M_MARRY_SUCESS.replace(Constants.TARGET,playerMarryPropose.getHusbandDisplayName()).replace(Constants.PLAYER, playerMarryPropose.getWifeDisplayName()));
                marrySucess(playerMarryPropose);
                Vars.proposesId.remove(Vars.marryProposes.get(id).getHusbandName());
                Vars.proposesId.remove(Vars.marryProposes.get(id).getWifeName());
                Vars.marryProposes.remove(id);
            } else {
                Vars.marryProposes.put(id, playerMarryPropose);
                Bukkit.broadcastMessage(Strings.M_MARRY_ACCEPT.replace(Constants.PLAYER, player.getDisplayName()));
            }
        }else {
            player.sendMessage(Strings.M_MARRY_PROPOSAL);
        }
    }

    @Subcommand("deny")
    public void onDeny(Player player) {
        final String wifeName = player.getName();
        if (Vars.proposesId.containsKey(wifeName)) {
            final int id = Vars.proposesId.get(wifeName);
            Vars.proposesId.remove(Vars.marryProposes.get(id).getHusbandName());
            Vars.proposesId.remove(Vars.marryProposes.get(id).getWifeName());
            Vars.marryProposes.remove(id);
            Bukkit.broadcastMessage(Strings.M_MARRY_DENY.replace(Constants.PLAYER, player.getDisplayName()));
        } else {
            player.sendMessage(Strings.M_MARRY_PROPOSAL);
        }
    }

    @Subcommand("deposit")
    @Syntax("<quantia>")
    public void onDeposit(Player player, Double amount) {
        final UUID uuid = UUIDFetcher.getUUIDOf(player.getName());
        if (APIMarry.isMarried(uuid)) {
            if (amount > 0) {
                if (economy.has(player, amount)) {
                    APIMarry.giveMarryBankMoney(APIMarry.getMarryId(uuid), amount);
                    economy.withdrawPlayer(player, amount);
                    player.sendMessage(Strings.M_COMMANDS_DEPOSIT.replace(Constants.AMOUNT, String.valueOf(amount)));
                } else {
                    player.sendMessage(Strings.M_NO_BAL);
                }
            } else {
                player.sendMessage(Strings.M_NO_MONEY);
            }
        } else {
            player.sendMessage(Strings.M_COMMANDS_NO_MARRY);
        }
    }

    @Subcommand("balance")
    public void onBalance(Player player) {
        final UUID uuid = UUIDFetcher.getUUIDOf(player.getName());
        if (APIMarry.isMarried(uuid)) {
            player.sendMessage(Strings.M_COMMANDS_BALANCE.replace(Constants.AMOUNT, String.valueOf(Vars.marrieds.get(APIMarry.getMarryId(uuid)).getMarryBalance())));
        } else {
            player.sendMessage(Strings.M_COMMANDS_NO_MARRY);
        }
    }

    @Subcommand("give")
    public void onGiveItem(Player player) {
        final UUID uuid = UUIDFetcher.getUUIDOf(player.getName());
        if (APIMarry.isMarried(uuid)) {
            final Player partner = Bukkit.getPlayer(APIMarry.getPartnerUUID(uuid));
            if (partner != null && partner.isOnline()) {
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack != air) {
                    giveItem(player, partner, itemStack);
                }
            } else {
                player.sendMessage(Strings.M_COMMANDS_OFFLINE);
            }
        } else {
            player.sendMessage(Strings.M_COMMANDS_NO_MARRY);
        }
    }

    @Subcommand("withdraw")
    @Syntax("<quantia>")
    public void onWithdraw(Player player, Double amount) {
        final UUID uuid = UUIDFetcher.getUUIDOf(player.getName());
        if (APIMarry.isMarried(uuid)) {
            if (amount > 0) {
                final int id = APIMarry.getMarryId(uuid);
                if (APIMarry.getMarryMoney(id) >= amount) {
                    APIMarry.removeMarryBankMoney(id, amount);
                    economy.depositPlayer(player, amount);
                    player.sendMessage(Strings.M_COMMANDS_DEPOSIT.replace(Constants.AMOUNT, String.valueOf(amount)));
                } else {
                    player.sendMessage(Strings.M_COMMANDS_NO_MONEY.replace(Constants.MONEY, String.valueOf(amount)));
                }
            } else {
                player.sendMessage(Strings.M_NO_MONEY);
            }
        } else {
            player.sendMessage(Strings.M_COMMANDS_NO_MARRY);
        }
    }

    @Subcommand("home")
    public void onHome(Player player) {
        final UUID uuid = UUIDFetcher.getUUIDOf(player.getName());
        if (APIMarry.isMarried(uuid)) {
            final Location location = Vars.marrieds.get(Vars.marriedUsers.get(uuid).getMarryId()).getMarryLocation();
            if (location != error) {
                Vars.teleports.put(player, new PlayerTeleport(player, location, Strings.M_COMMANDS_DONE));
            } else {
                player.sendMessage(Strings.M_COMMANDS_NO_HOME);
            }
        } else {
            player.sendMessage(Strings.M_COMMANDS_NO_MARRY);
        }
    }

    @Subcommand("sethome")
    public void onSetHome(Player player) {
        final UUID uuid = UUIDFetcher.getUUIDOf(player.getName());
        if (Vars.marriedUsers.containsKey(uuid)) {
            final MarryId marryId = Vars.marrieds.get(Vars.marriedUsers.get(uuid).getMarryId());

            final double setHomeCost = EterniaMarriage.serverConfig.getDouble("money.sethome");
            final double marryMoney = marryId.getMarryBalance();

            if (marryMoney >= setHomeCost) {
                final int id = marryId.getMarryId();
                final Location loc = player.getLocation();
                final String saveloc = loc.getWorld().getName() +
                        ":" + ((int) loc.getX()) +
                        ":" + ((int) loc.getY()) +
                        ":" + ((int) loc.getZ()) +
                        ":" + ((int) loc.getYaw()) +
                        ":" + ((int) loc.getPitch());

                marryId.setMarryLocation(loc);
                marryId.setMarryBalance(marryId.getMarryBalance() - setHomeCost);
                Vars.marrieds.put(id, marryId);

                EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_BANK, Strings.LOC, saveloc, Strings.MARRY_ID, id), false);
                player.sendMessage(Strings.M_COMMANDS_HOME_SAVE.replace(Constants.MONEY, String.valueOf(setHomeCost)));
            } else {
                player.sendMessage(Strings.M_COMMANDS_NO_MONEY.replace(Constants.MONEY, String.valueOf(setHomeCost)));
            }
        } else {
            player.sendMessage(Strings.M_COMMANDS_NO_MARRY);
        }
    }

    private void giveItem(Player player, Player target, ItemStack itemStack) {
        target.getInventory().addItem(itemStack);
        player.getInventory().setItemInMainHand(air);
    }

    private void marrySucess(final PlayerMarryPropose playerMarryPropose) {
        final long time = System.currentTimeMillis();
        EQueries.executeQuery(Constants.getQueryInsert(Constants.TABLE_BANK, "(" + Strings.MARRY_ID
                + ", " + Strings.BALANCE
                + ", " + Strings.HOURS
                + ", " + Strings.LOC
                + ", " + Strings.TIME
                + ", " + Strings.LAST + ")", "('"
                + (Vars.marrieds.size() + 1) + "', '"
                + 0 + "', '"
                + 0 + "', 'world:666:666:666:666:666', '"
                + time + "', '"
                + time + "')"));

        save(playerMarryPropose, Vars.marrieds.size() + 1);
        Vars.marrieds.put(Vars.marrieds.size() + 1, new MarryId(Vars.marrieds.size() + 1, 0.0, 0, error, time, time));
    }

    private void save(final PlayerMarryPropose playerMarryPropose, int id) {

        EQueries.executeQuery(Constants.getQueryInsert(Constants.TABLE_MARRY, "(uuid, marry_uuid, marry_name, marry_display, marry_id)",
                "('" + playerMarryPropose.getWifeUUID().toString() + "', '" + playerMarryPropose.getHusbandUUID().toString() + "', '" + playerMarryPropose.getHusbandName() + "', '" + playerMarryPropose.getHusbandDisplayName() + "', '" + id + "')"));
        EQueries.executeQuery(Constants.getQueryInsert(Constants.TABLE_MARRY, "(uuid, marry_uuid, marry_name, marry_display, marry_id)",
                "('" + playerMarryPropose.getHusbandUUID().toString() + "', '" + playerMarryPropose.getWifeUUID().toString() + "', '" + playerMarryPropose.getWifeName() + "', '" + playerMarryPropose.getWifeDisplayName() + "', '" + id + "')"));


        Vars.userKiss.put(playerMarryPropose.getWifeUUID(), System.currentTimeMillis());
        Vars.userKiss.put(playerMarryPropose.getHusbandUUID(), System.currentTimeMillis());
        Vars.marriedUsers.put(playerMarryPropose.getWifeUUID(), new PlayerMarry(playerMarryPropose.getWifeUUID(), playerMarryPropose.getHusbandUUID(), playerMarryPropose.getHusbandName(), playerMarryPropose.getHusbandDisplayName(), id));
        Vars.marriedUsers.put(playerMarryPropose.getHusbandUUID(), new PlayerMarry(playerMarryPropose.getHusbandUUID(), playerMarryPropose.getWifeUUID(), playerMarryPropose.getWifeName(), playerMarryPropose.getWifeDisplayName(), id));
    }

    private void marryDeny(final UUID wifeUUID, final UUID husbandUUID) {
        final int id = APIMarry.getMarryId(wifeUUID);

        EQueries.executeQuery(Constants.getQueryDelete(Constants.TABLE_BANK, Strings.MARRY_ID, String.valueOf(id)));
        EQueries.executeQuery(Constants.getQueryDelete(Constants.TABLE_MARRY, Strings.UUID, wifeUUID.toString()));
        EQueries.executeQuery(Constants.getQueryDelete(Constants.TABLE_MARRY, Strings.UUID, husbandUUID.toString()));

        Vars.marrieds.remove(id);
        Vars.marriedUsers.remove(wifeUUID);
        Vars.marriedUsers.remove(husbandUUID);
    }

    private void sendConsole(String msg) {
        Bukkit.getConsoleSender().sendMessage(msg);
    }

}
