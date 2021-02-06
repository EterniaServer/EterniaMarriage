package br.com.eterniaserver.eterniamarriage.core;

import br.com.eterniaserver.acf.BaseCommand;
import br.com.eterniaserver.acf.annotation.*;
import br.com.eterniaserver.acf.bukkit.contexts.OnlinePlayer;
import br.com.eterniaserver.eternialib.NBTItem;
import br.com.eterniaserver.eternialib.SQL;
import br.com.eterniaserver.eternialib.UUIDFetcher;
import br.com.eterniaserver.eternialib.sql.queries.Delete;
import br.com.eterniaserver.eternialib.sql.queries.Insert;
import br.com.eterniaserver.eternialib.sql.queries.Select;
import br.com.eterniaserver.eternialib.sql.queries.Update;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.enums.Doubles;
import br.com.eterniaserver.eterniamarriage.enums.Messages;
import br.com.eterniaserver.eterniamarriage.enums.Strings;
import br.com.eterniaserver.eterniamarriage.objects.MarryId;
import br.com.eterniaserver.eterniamarriage.objects.PlayerMarry;
import br.com.eterniaserver.eterniamarriage.objects.PlayerMarryPropose;
import br.com.eterniaserver.eterniamarriage.objects.PlayerTeleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandAlias("marry")
@CommandPermission("eternia.marry")
public class MarryCommand extends BaseCommand {

    private final ItemStack air = new ItemStack(Material.AIR);
    private final Location error;

    public MarryCommand() {
        error = new Location(Bukkit.getWorld("world"), 666, 666, 666, 666, 666);

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(new Select(EterniaMarriage.getString(Strings.TABLE_MARRY)).queryString()); ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                final UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                final UUID marryUUID = UUID.fromString(resultSet.getString("marry_uuid"));
                Vars.marriedUsers.put(uuid, new PlayerMarry(
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

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(new Select(EterniaMarriage.getString(Strings.TABLE_BANK)).queryString()); ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                final int marryId = resultSet.getInt("marry_id");
                final String[] split = resultSet.getString("location").split(":");
                final Location loc = new Location(Bukkit.getWorld(split[0]),
                        Double.parseDouble(split[1]),
                        (Double.parseDouble(split[2]) + 1),
                        Double.parseDouble(split[3]),
                        Float.parseFloat(split[4]),
                        Float.parseFloat(split[5]));

                Vars.marrieds.put(marryId, new MarryId(
                        marryId,
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

        sendConsole(EterniaMarriage.getMessage(Messages.SERVER_LOADED, true, "Married Users", String.valueOf(Vars.marriedUsers.size())));
        sendConsole(EterniaMarriage.getMessage(Messages.SERVER_LOADED, true, "Marry Accounts", String.valueOf(Vars.marrieds.size())));

    }

    @Subcommand("marry")
    @Syntax("<jogador(a)> <jogador(a)>")
    @CommandCompletion("@players @players")
    @CommandPermission("eternia.priest")
    public void onMarry(Player player, OnlinePlayer marryOne, OnlinePlayer marryTwo) {
        final double money = EterniaMarriage.getDouble(Doubles.MARRY_COST);

        if (!EterniaMarriage.getEcon().has(player, money)) {
            EterniaMarriage.sendMessage(player, Messages.SERVER_NO_MONEY, String.valueOf(money));
            return;
        }

        final Player wife = marryOne.getPlayer();
        final Player husband = marryTwo.getPlayer();
        final String wifeName = wife.getName();
        final String husbandName = husband.getName();

        if (wifeName.equals(husbandName)) {
            EterniaMarriage.sendMessage(player, Messages.SERVER_NOT_SAME);
            return;
        }

        final UUID wifeUUID = wife.getUniqueId();
        final UUID husbandUUID = husband.getUniqueId();
        if (APIMarry.isMarried(wifeUUID) || APIMarry.isMarried(husbandUUID)) {
            EterniaMarriage.sendMessage(player, Messages.MARRY_ALREADY_MARRIED);
            return;
        }

        if (Vars.proposesId.containsKey(wifeName) || Vars.proposesId.containsKey(husbandName)) {
            EterniaMarriage.sendMessage(player, Messages.MARRY_ALREADY_SENT);
            return;
        }

        
        Bukkit.broadcastMessage(EterniaMarriage.getMessage(Messages.MARRY_ADVICE, true, wife.getName(), wife.getDisplayName(), husband.getName(), husband.getDisplayName()));
        EterniaMarriage.sendMessage(wife, Messages.MARRY_SEND_PROPOSAL, wife.getName(), wife.getDisplayName(), husband.getName(), husband.getDisplayName());
        EterniaMarriage.sendMessage(husband, Messages.MARRY_SEND_PROPOSAL, husband.getName(), husband.getDisplayName(), wife.getName(), wife.getDisplayName());

        EterniaMarriage.getEcon().withdrawPlayer(player, money);
        PlayerMarryPropose marryPropose = new PlayerMarryPropose(wifeUUID, husbandUUID);
        marryPropose.time = System.currentTimeMillis();
        Vars.marryProposes.put(Vars.marrieds.size() + 1, marryPropose);
        Vars.proposesId.put(wifeName, Vars.marrieds.size() + 1);
        Vars.proposesId.put(husbandName, Vars.marrieds.size() + 1);
    }

    @Subcommand("reload")
    @CommandPermission("eternia.reload")
    public void onReload(Player player) {
        EterniaMarriage.loadConfigurations();
        EterniaMarriage.sendMessage(player, Messages.SERVER_RELOAD);
    }

    @Subcommand("divorce")
    @Syntax("<jogador(a)> <jogador(a)>")
    @CommandCompletion("@players @players")
    @CommandPermission("eternia.priest")
    public void onDivorce(Player player, String wifeName, String husbandName) {
        CompletableFuture.runAsync(() -> {
            final UUID wifeUUID = UUIDFetcher.getUUIDOf(wifeName);
            final UUID husbandUUID = UUIDFetcher.getUUIDOf(husbandName);
            if (APIMarry.isMarried(wifeUUID) && APIMarry.getPartnerUUID(wifeUUID).equals(husbandUUID)) {
                marryDeny(wifeUUID, husbandUUID);
                return;
            }
            EterniaMarriage.sendMessage(player, Messages.MARRY_NO_MARRY);
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
                Bukkit.broadcastMessage(EterniaMarriage.getMessage(Messages.MARRY_SUCESS, true, playerMarryPropose.getHusbandName(), playerMarryPropose.getHusbandDisplayName(), playerMarryPropose.getWifeName(), playerMarryPropose.getWifeDisplayName()));
                marrySucess(playerMarryPropose);
                Vars.proposesId.remove(Vars.marryProposes.get(id).getHusbandName());
                Vars.proposesId.remove(Vars.marryProposes.get(id).getWifeName());
                Vars.marryProposes.remove(id);
            } else {
                playerMarryPropose.time = System.currentTimeMillis();
                Vars.marryProposes.put(id, playerMarryPropose);
                Bukkit.broadcastMessage(EterniaMarriage.getMessage(Messages.MARRY_ACCEPT, true, player.getName(), player.getDisplayName()));
            }
        }else {
            EterniaMarriage.sendMessage(player, Messages.MARRY_NO_PROPOSAL);
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
            Bukkit.broadcastMessage(EterniaMarriage.getMessage(Messages.MARRY_DENY, true, player.getName(), player.getDisplayName()));
        } else {
            EterniaMarriage.sendMessage(player, Messages.MARRY_NO_PROPOSAL);
        }
    }

    @Subcommand("deposit")
    @Syntax("<quantia>")
    public void onDeposit(Player player, Double amount) {
        final UUID uuid = player.getUniqueId();
        if (APIMarry.isMarried(uuid)) {
            if (amount > 0) {
                if (EterniaMarriage.getEcon().has(player, amount)) {
                    APIMarry.giveMarryBankMoney(APIMarry.getMarryId(uuid), amount);
                    EterniaMarriage.getEcon().withdrawPlayer(player, amount);
                    EterniaMarriage.sendMessage(player, Messages.COMMANDS_DEPOSIT, String.valueOf(amount));
                } else {
                    EterniaMarriage.sendMessage(player, Messages.SERVER_NO_MONEY, String.valueOf(amount));
                }
            } else {
                EterniaMarriage.sendMessage(player, Messages.SERVER_NO_MONEY, String.valueOf(amount));
            }
        } else {
            EterniaMarriage.sendMessage(player, Messages.COMMANDS_NOT_MARRIED);
        }
    }

    @Subcommand("balance")
    public void onBalance(Player player) {
        final UUID uuid = player.getUniqueId();
        if (APIMarry.isMarried(uuid)) {
            EterniaMarriage.sendMessage(player, Messages.COMMANDS_BALANCE, String.valueOf(Vars.marrieds.get(APIMarry.getMarryId(uuid)).getMarryBalance()));
        } else {
            EterniaMarriage.sendMessage(player, Messages.COMMANDS_NOT_MARRIED);
        }
    }

    @Subcommand("give")
    public void onGiveItem(Player player) {
        final UUID uuid = player.getUniqueId();
        if (APIMarry.isMarried(uuid)) {
            final Player partner = Bukkit.getPlayer(APIMarry.getPartnerUUID(uuid));
            if (partner != null && partner.isOnline()) {
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack != air) {
                    if (new NBTItem(player.getInventory().getItemInMainHand()).getInteger("EterniaLock") == 1) {
                        return;
                    }
                    giveItem(player, partner, itemStack);
                }
            } else {
                EterniaMarriage.sendMessage(player, Messages.COMMANDS_OFFLINE);
            }
        } else {
            EterniaMarriage.sendMessage(player, Messages.COMMANDS_NOT_MARRIED);
        }
    }

    @Subcommand("withdraw")
    @Syntax("<quantia>")
    public void onWithdraw(Player player, Double amount) {
        final UUID uuid = player.getUniqueId();
        if (APIMarry.isMarried(uuid)) {
            if (amount > 0) {
                final int id = APIMarry.getMarryId(uuid);
                if (APIMarry.getMarryMoney(id) >= amount) {
                    APIMarry.removeMarryBankMoney(id, amount);
                    EterniaMarriage.getEcon().depositPlayer(player, amount);
                    EterniaMarriage.sendMessage(player, Messages.COMMANDS_WITHDRAW, String.valueOf(amount));
                } else {
                    EterniaMarriage.sendMessage(player, Messages.COMMANDS_NOT_MONEY, String.valueOf(amount));
                }
            } else {
                EterniaMarriage.sendMessage(player, Messages.SERVER_NO_MONEY, "1");
            }
        } else {
            EterniaMarriage.sendMessage(player, Messages.COMMANDS_NOT_MARRIED);
        }
    }

    @Subcommand("home")
    public void onHome(Player player) {
        final UUID uuid = player.getUniqueId();
        if (APIMarry.isMarried(uuid)) {
            final Location location = Vars.marrieds.get(Vars.marriedUsers.get(uuid).getMarryId()).getMarryLocation();
            if (location != error) {
                
                Vars.teleports.put(player, new PlayerTeleport(player, location, EterniaMarriage.getMessage(Messages.COMMANDS_HOME, true)));
            } else {
                EterniaMarriage.sendMessage(player, Messages.COMMANDS_NO_HOME);
            }
        } else {
            EterniaMarriage.sendMessage(player, Messages.COMMANDS_NOT_MARRIED);
        }
    }

    @Subcommand("sethome")
    public void onSetHome(Player player) {
        final UUID uuid = player.getUniqueId();
        if (Vars.marriedUsers.containsKey(uuid)) {
            final MarryId marryId = Vars.marrieds.get(Vars.marriedUsers.get(uuid).getMarryId());
            final double setHomeCost = EterniaMarriage.getDouble(Doubles.SETHOME_COST);
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

                Update update = new Update(EterniaMarriage.getString(Strings.TABLE_BANK));
                update.set.set("location", saveloc);
                update.where.set("marry_id", id);
                SQL.executeAsync(update);
                EterniaMarriage.sendMessage(player, Messages.COMMANDS_HOME_SAVE, String.valueOf(setHomeCost));
            } else {
                EterniaMarriage.sendMessage(player, Messages.COMMANDS_NOT_MONEY, String.valueOf(marryMoney));
            }
        } else {
            EterniaMarriage.sendMessage(player, Messages.COMMANDS_NOT_MARRIED);
        }
    }

    private void giveItem(Player player, Player target, ItemStack itemStack) {
        target.getInventory().addItem(itemStack);
        player.getInventory().setItemInMainHand(air);
    }

    private void marrySucess(final PlayerMarryPropose playerMarryPropose) {
        final long time = System.currentTimeMillis();

        Insert insert = new Insert(EterniaMarriage.getString(Strings.TABLE_BANK));
        insert.columns.set("marry_id", "balance", "hours", "location", "time", "last");
        insert.values.set((Vars.marrieds.size() + 1), 0, 0, "world:666:666:666:666:666", time, time);
        SQL.executeAsync(insert);

        save(playerMarryPropose, Vars.marrieds.size() + 1);
        Vars.marrieds.put(Vars.marrieds.size() + 1, new MarryId(Vars.marrieds.size() + 1, 0.0, 0, error, time, time));
    }

    private void save(final PlayerMarryPropose playerMarryPropose, int id) {

        Insert insert = new Insert(EterniaMarriage.getString(Strings.TABLE_MARRY));
        insert.columns.set("uuid", "marry_uuid", "marry_name", "marry_display", "marry_id");
        insert.values.set(playerMarryPropose.getWifeUUID().toString(), playerMarryPropose.getHusbandUUID().toString(), playerMarryPropose.getHusbandName(), playerMarryPropose.getHusbandDisplayName(), id);
        SQL.executeAsync(insert);

        insert = new Insert(EterniaMarriage.getString(Strings.TABLE_MARRY));
        insert.columns.set("uuid", "marry_uuid", "marry_name", "marry_display", "marry_id");
        insert.values.set(playerMarryPropose.getHusbandUUID().toString(), playerMarryPropose.getWifeUUID().toString(), playerMarryPropose.getWifeName(), playerMarryPropose.getWifeDisplayName(), id);
        SQL.executeAsync(insert);

        Vars.userKiss.put(playerMarryPropose.getWifeUUID(), System.currentTimeMillis());
        Vars.userKiss.put(playerMarryPropose.getHusbandUUID(), System.currentTimeMillis());
        Vars.marriedUsers.put(playerMarryPropose.getWifeUUID(), new PlayerMarry(playerMarryPropose.getWifeUUID(), playerMarryPropose.getHusbandUUID(), playerMarryPropose.getHusbandName(), playerMarryPropose.getHusbandDisplayName(), id));
        Vars.marriedUsers.put(playerMarryPropose.getHusbandUUID(), new PlayerMarry(playerMarryPropose.getHusbandUUID(), playerMarryPropose.getWifeUUID(), playerMarryPropose.getWifeName(), playerMarryPropose.getWifeDisplayName(), id));
    }

    private void marryDeny(final UUID wifeUUID, final UUID husbandUUID) {
        final int id = APIMarry.getMarryId(wifeUUID);

        Delete delete = new Delete(EterniaMarriage.getString(Strings.TABLE_BANK));
        delete.where.set("marry_id", id);
        SQL.executeAsync(delete);

        delete = new Delete(EterniaMarriage.getString(Strings.TABLE_MARRY));
        delete.where.set("uuid", wifeUUID.toString());
        SQL.executeAsync(delete);

        delete = new Delete(EterniaMarriage.getString(Strings.TABLE_MARRY));
        delete.where.set("uuid", husbandUUID.toString());
        SQL.executeAsync(delete);

        Vars.marrieds.remove(id);
        Vars.marriedUsers.remove(wifeUUID);
        Vars.marriedUsers.remove(husbandUUID);
    }

    private void sendConsole(String msg) {
        Bukkit.getConsoleSender().sendMessage(msg);
    }

}
