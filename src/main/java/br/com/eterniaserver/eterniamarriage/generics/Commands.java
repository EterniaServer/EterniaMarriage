package br.com.eterniaserver.eterniamarriage.generics;

import br.com.eterniaserver.acf.BaseCommand;
import br.com.eterniaserver.acf.annotation.*;
import br.com.eterniaserver.acf.bukkit.contexts.OnlinePlayer;
import br.com.eterniaserver.eternialib.EFiles;
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

    private int marryNumber = 0;

    private final Economy economy;
    private final EterniaMarriage plugin;
    private final ItemStack air = new ItemStack(Material.AIR);
    private final EFiles messages;
    private final Location error;

    public Commands(EterniaMarriage plugin) {
        error = new Location(Bukkit.getWorld("world"), 666, 666, 666, 666, 666);
        this.plugin = plugin;
        this.messages = plugin.getEFiles();
        this.economy = plugin.getEcon();

        if (EterniaLib.getMySQL()) {
            EterniaLib.getPlugin().connections.executeSQLQuery(connection -> {
                final PreparedStatement getHashMap = connection.prepareStatement(Constants.getQuerySelectAll(Constants.TABLE_MARRY));
                final ResultSet resultSet = getHashMap.executeQuery();
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
                getHashMap.close();
                resultSet.close();

                final PreparedStatement getHashMaps = connection.prepareStatement(Constants.getQuerySelectAll(Constants.TABLE_BANK));
                final ResultSet resultSets = getHashMaps.executeQuery();
                while (resultSets.next()) {
                    final int marryId = resultSets.getInt(Strings.MARRY_ID);
                    final String[] split = resultSets.getString(Strings.LOC).split(":");
                    final Location loc = new Location(Bukkit.getWorld(split[0]),
                            Double.parseDouble(split[1]),
                            (Double.parseDouble(split[2]) + 1),
                            Double.parseDouble(split[3]),
                            Float.parseFloat(split[4]),
                            Float.parseFloat(split[5]));
                    Vars.marrieds.put(marryId, new MarryId(
                            marryId,
                            resultSets.getDouble(Strings.BALANCE),
                            resultSets.getInt(Strings.HOURS),
                            loc,
                            resultSets.getLong(Strings.TIME),
                            resultSets.getLong(Strings.LAST)
                    ));
                }
                getHashMaps.close();
                resultSets.close();
            });
        } else {
            try {
                final PreparedStatement getHashMap = Connections.connection.prepareStatement(Constants.getQuerySelectAll(Constants.TABLE_MARRY));
                final ResultSet resultSet = getHashMap.executeQuery();
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
                getHashMap.close();
                resultSet.close();

                final PreparedStatement getHashMaps = Connections.connection.prepareStatement(Constants.getQuerySelectAll(Constants.TABLE_BANK));
                final ResultSet resultSets = getHashMaps.executeQuery();
                while (resultSets.next()) {
                    final int marryId = resultSets.getInt(Strings.MARRY_ID);
                    final String[] split = resultSets.getString(Strings.LOC).split(":");
                    final Location loc = new Location(Bukkit.getWorld(split[0]),
                            Double.parseDouble(split[1]),
                            (Double.parseDouble(split[2]) + 1),
                            Double.parseDouble(split[3]),
                            Float.parseFloat(split[4]),
                            Float.parseFloat(split[5]));
                    Vars.marrieds.put(marryId, new MarryId(
                            marryId,
                            resultSets.getDouble(Strings.BALANCE),
                            resultSets.getInt(Strings.HOURS),
                            loc,
                            resultSets.getLong(Strings.TIME),
                            resultSets.getLong(Strings.LAST)
                    ));
                }
                getHashMaps.close();
                resultSets.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        messages.sendConsole(Strings.M_SERVER_LOAD, Constants.MODULE, "Married Users", Constants.AMOUNT, Vars.marriedUsers.size());
        messages.sendConsole(Strings.M_SERVER_LOAD, Constants.MODULE, "Marry Accounts", Constants.AMOUNT, Vars.marrieds.size());

    }

    @Default
    @Syntax("<jogador(a)> <jogador(a)>")
    @CommandCompletion("@players @players")
    @CommandPermission("eternia.priest")
    public void onMarry(Player player, OnlinePlayer marryOne, OnlinePlayer marryTwo) {
        final double money = EterniaMarriage.serverConfig.getDouble("money.marry");

        if (!economy.has(player, money)) {
            messages.sendMessage(Strings.M_BALANCE_NO, Constants.MONEY, money, player);
            return;
        }

        final Player wife = marryOne.getPlayer();
        final Player husband = marryTwo.getPlayer();
        final String wifeName = wife.getName();
        final String husbandName = husband.getName();

        if (wifeName.equals(husbandName)) {
            messages.sendMessage(Strings.M_SERVER_YOUR, player);
            return;
        }

        final UUID wifeUUID = UUIDFetcher.getUUIDOf(wifeName);
        final UUID husbandUUID = UUIDFetcher.getUUIDOf(husbandName);
        if (APIMarry.isMarried(wifeUUID) || APIMarry.isMarried(husbandUUID)) {
            messages.sendMessage(Strings.M_MARRY_ALREADY, player);
            return;
        }

        if (Vars.proposesId.containsKey(wifeName) || Vars.proposesId.containsKey(husbandName)) {
            messages.sendMessage(Strings.M_MARRY_ALREADY_SENT, player);
            return;
        }

        messages.broadcastMessage(Strings.M_MARRY_ADVICE, Constants.PLAYER, wife.getDisplayName(), Constants.TARGET, husband.getDisplayName());
        messages.sendMessage(Strings.M_MARRY_SENT, Constants.PLAYER, wife.getDisplayName(), Constants.TARGET, husband.getDisplayName(), wife);
        messages.sendMessage(Strings.M_MARRY_SENT, Constants.PLAYER, husband.getDisplayName(), Constants.TARGET, wife.getDisplayName(), husband);

        economy.withdrawPlayer(player, money);
        Vars.marryProposes.put(marryNumber, new PlayerMarryPropose(wifeUUID, husbandUUID));
        Vars.proposesId.put(wifeName, marryNumber);
        Vars.proposesId.put(husbandName, marryNumber);
        marryNumber++;
    }

    @Subcommand("reload")
    @CommandPermission("eternia.reload")
    public void onReload(Player player) {
        plugin.getFiles().loadConfigs();
        plugin.getFiles().loadDatabase();
        plugin.getFiles().loadMessages();
        messages.sendMessage(Strings.M_SERVER_RELOAD, player);
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
            messages.sendMessage(Strings.M_MARRY_NO, player);
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
                messages.broadcastMessage(Strings.M_MARRY_SUCESS, Constants.TARGET, playerMarryPropose.getHusbandDisplayName(), Constants.PLAYER, playerMarryPropose.getWifeDisplayName());
                marrySucess(playerMarryPropose);
                Vars.proposesId.remove(Vars.marryProposes.get(id).getHusbandName());
                Vars.proposesId.remove(Vars.marryProposes.get(id).getWifeName());
                Vars.marryProposes.remove(id);
            } else {
                Vars.marryProposes.put(id, playerMarryPropose);
                messages.broadcastMessage(Strings.M_MARRY_ACCEPT, Constants.PLAYER, player.getDisplayName());
            }
        }else {
            messages.sendMessage(Strings.M_MARRY_PROPOSAL, player);
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
            messages.broadcastMessage(Strings.M_MARRY_DENY, Constants.PLAYER, player.getDisplayName());
        } else {
            messages.sendMessage(Strings.M_MARRY_PROPOSAL, player);
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
                    messages.sendMessage(Strings.M_COMMANDS_DEPOSIT, Constants.AMOUNT, amount, player);
                } else {
                    messages.sendMessage(Strings.M_NO_BAL, player);
                }
            } else {
                messages.sendMessage(Strings.M_NO_MONEY, player);
            }
        } else {
            messages.sendMessage(Strings.M_COMMANDS_NO_MARRY, player);
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
                messages.sendMessage(Strings.M_COMMANDS_OFFLINE, player);
            }
        } else {
            messages.sendMessage(Strings.M_COMMANDS_NO_MARRY, player);
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
                    messages.sendMessage(Strings.M_COMMANDS_DEPOSIT, Constants.AMOUNT, amount, player);
                } else {
                    messages.sendMessage(Strings.M_COMMANDS_NO_MONEY, Constants.MONEY, amount, player);
                }
            } else {
                messages.sendMessage(Strings.M_NO_MONEY, player);
            }
        } else {
            messages.sendMessage(Strings.M_COMMANDS_NO_MARRY, player);
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
                messages.sendMessage(Strings.M_COMMANDS_NO_HOME, player);
            }
        } else {
            messages.sendMessage(Strings.M_COMMANDS_NO_MARRY, player);
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
                messages.sendMessage(Strings.M_COMMANDS_HOME_SAVE, Constants.MONEY, setHomeCost, player);
            } else {
                messages.sendMessage(Strings.M_COMMANDS_NO_MONEY, Constants.MONEY, setHomeCost, player);
            }
        } else {
            messages.sendMessage(Strings.M_COMMANDS_NO_MARRY, player);
        }
    }

    private void giveItem(Player player, Player target, ItemStack itemStack) {
        target.getInventory().addItem(itemStack);
        player.getInventory().setItemInMainHand(air);
    }

    private void marrySucess(final PlayerMarryPropose playerMarryPropose) {
        final long time = System.currentTimeMillis();
        Vars.marryIdList = Vars.marryIdList + 1;

        final int id = Vars.marryIdList;
        EQueries.executeQuery(Constants.getQueryInsert(Constants.TABLE_BANK, "(" + Strings.MARRY_ID
                + ", " + Strings.BALANCE
                + ", " + Strings.HOURS
                + ", " + Strings.LOC
                + ", " + Strings.TIME
                + ", " + Strings.LAST + ")", "('"
                + id + "', '"
                + 0 + "', '"
                + 0 + "', 'world:666:666:666:666:666', '"
                + time + "', '"
                + time + "')"));

        Vars.marrieds.put(id, new MarryId(id, 0.0, 0, error, time, time));

        save(playerMarryPropose, Vars.marryIdList);
    }

    private void save(final PlayerMarryPropose playerMarryPropose, int id) {

        EQueries.executeQuery(Constants.getQueryInsert(Constants.TABLE_MARRY, "(uuid, marry_uuid, marry_name, marry_display, marry_id)",
                "('" + playerMarryPropose.getWifeUUID().toString() + "', '" + playerMarryPropose.getHusbandUUID().toString() + "', '" + playerMarryPropose.getHusbandName() + "', '" + playerMarryPropose.getHusbandDisplayName() + "', '" + id + "')"));
        EQueries.executeQuery(Constants.getQueryInsert(Constants.TABLE_MARRY, "(uuid, marry_uuid, marry_name, marry_display, marry_id)",
                "('" + playerMarryPropose.getHusbandUUID().toString() + "', '" + playerMarryPropose.getWifeUUID().toString() + "', '" + playerMarryPropose.getWifeName() + "', '" + playerMarryPropose.getWifeDisplayName() + "', '" + id + "')"));


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


}
