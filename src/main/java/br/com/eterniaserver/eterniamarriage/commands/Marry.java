package br.com.eterniaserver.eterniamarriage.commands;

import br.com.eterniaserver.acf.BaseCommand;
import br.com.eterniaserver.acf.annotation.*;
import br.com.eterniaserver.acf.bukkit.contexts.OnlinePlayer;
import br.com.eterniaserver.eternialib.EterniaLib;
import br.com.eterniaserver.eternialib.SQL;
import br.com.eterniaserver.eternialib.UUIDFetcher;
import br.com.eterniaserver.eternialib.core.queries.Delete;
import br.com.eterniaserver.eternialib.core.queries.Insert;
import br.com.eterniaserver.eternialib.core.queries.Update;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.core.Manager;
import br.com.eterniaserver.eterniamarriage.core.enums.Doubles;
import br.com.eterniaserver.eterniamarriage.core.enums.Messages;
import br.com.eterniaserver.eterniamarriage.core.enums.Strings;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.MarryId;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.PlayerMarry;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.PlayerMarryPropose;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.PlayerTeleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@CommandAlias("marry")
@CommandPermission("eternia.marry")
public class Marry extends BaseCommand {

    private final EterniaMarriage plugin;
    private final Manager manager;
    private final ItemStack air;

    private Location error;

    public Marry(final EterniaMarriage plugin, final Manager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.air = new ItemStack(Material.AIR);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
                () -> error = new Location(Bukkit.getWorld("world"), 666, 666, 666, 666, 666));
    }

    @Subcommand("marry")
    @Syntax("<jogador(a)> <jogador(a)>")
    @CommandCompletion("@players @players")
    @CommandPermission("eternia.priest")
    public void onMarry(Player player, OnlinePlayer marryOne, OnlinePlayer marryTwo) {
        final double money = plugin.getDouble(Doubles.MARRY_COST);

        if (!plugin.economy.has(player, money)) {
            plugin.sendMessage(player, Messages.SERVER_NO_MONEY, String.valueOf(money));
            return;
        }

        final Player wife = marryOne.getPlayer();
        final Player husband = marryTwo.getPlayer();
        final String wifeName = wife.getName();
        final String husbandName = husband.getName();

        if (wifeName.equals(husbandName)) {
            plugin.sendMessage(player, Messages.SERVER_NOT_SAME);
            return;
        }

        final UUID wifeUUID = wife.getUniqueId();
        final UUID husbandUUID = husband.getUniqueId();
        if (manager.isMarried(wifeUUID) || manager.isMarried(husbandUUID)) {
            plugin.sendMessage(player, Messages.MARRY_ALREADY_MARRIED);
            return;
        }

        if (plugin.proposesId.containsKey(wifeName) || plugin.proposesId.containsKey(husbandName)) {
            plugin.sendMessage(player, Messages.MARRY_ALREADY_SENT);
            return;
        }


        EterniaLib.report(plugin.getMessage(Messages.MARRY_ADVICE, true, wife.getName(), wife.getDisplayName(), husband.getName(), husband.getDisplayName()));
        plugin.sendMessage(wife, Messages.MARRY_SEND_PROPOSAL, wife.getName(), wife.getDisplayName(), husband.getName(), husband.getDisplayName());
        plugin.sendMessage(husband, Messages.MARRY_SEND_PROPOSAL, husband.getName(), husband.getDisplayName(), wife.getName(), wife.getDisplayName());

        plugin.economy.withdrawPlayer(player, money);
        PlayerMarryPropose marryPropose = new PlayerMarryPropose(wifeUUID, husbandUUID);
        marryPropose.time = System.currentTimeMillis();
        plugin.marryProposes.put(plugin.marrieds.size() + 1, marryPropose);
        plugin.proposesId.put(wifeName, plugin.marrieds.size() + 1);
        plugin.proposesId.put(husbandName, plugin.marrieds.size() + 1);
    }

    @Subcommand("divorce")
    @Syntax("<jogador(a)> <jogador(a)>")
    @CommandCompletion("@players @players")
    @CommandPermission("eternia.priest")
    public void onDivorce(Player player, String wifeName, String husbandName) {
        final UUID wifeUUID = UUIDFetcher.getUUIDOf(wifeName);
        final UUID husbandUUID = UUIDFetcher.getUUIDOf(husbandName);

        if (wifeUUID == null || husbandUUID == null) {
            plugin.sendMessage(player, Messages.MARRY_NO_MARRY);
        }

        if (manager.isMarried(wifeUUID) && plugin.marriedUsers.get(wifeUUID).getMarryUUID().equals(husbandUUID)) {
            marryDeny(wifeUUID, husbandUUID);
            return;
        }

        plugin.sendMessage(player, Messages.MARRY_NO_MARRY);
    }

    @Subcommand("accept")
    public void onAccept(Player player) {
        final String wifeName = player.getName();
        if (!plugin.proposesId.containsKey(wifeName)) {
            plugin.sendMessage(player, Messages.MARRY_NO_PROPOSAL);
            return;
        }

        final int id = plugin.proposesId.get(wifeName);
        final PlayerMarryPropose playerMarryPropose = plugin.marryProposes.get(id);
        playerMarryPropose.setMarryAccept();

        if (playerMarryPropose.getMarryAccept()) {
            EterniaLib.report(plugin.getMessage(Messages.MARRY_SUCESS, true, playerMarryPropose.getHusbandName(), playerMarryPropose.getHusbandDisplayName(), playerMarryPropose.getWifeName(), playerMarryPropose.getWifeDisplayName()));
            marrySucess(playerMarryPropose);
            plugin.proposesId.remove(plugin.marryProposes.get(id).getHusbandName());
            plugin.proposesId.remove(plugin.marryProposes.get(id).getWifeName());
            plugin.marryProposes.remove(id);
            return;
        }

        playerMarryPropose.time = System.currentTimeMillis();
        plugin.marryProposes.put(id, playerMarryPropose);
        EterniaLib.report(plugin.getMessage(Messages.MARRY_ACCEPT, true, player.getName(), player.getDisplayName()));
    }

    @Subcommand("deny")
    public void onDeny(Player player) {
        final String wifeName = player.getName();
        if (!plugin.proposesId.containsKey(wifeName)) {
            plugin.sendMessage(player, Messages.MARRY_NO_PROPOSAL);
            return;
        }

        final int id = plugin.proposesId.get(wifeName);
        plugin.proposesId.remove(plugin.marryProposes.get(id).getHusbandName());
        plugin.proposesId.remove(plugin.marryProposes.get(id).getWifeName());
        plugin.marryProposes.remove(id);
        EterniaLib.report(plugin.getMessage(Messages.MARRY_DENY, true, player.getName(), player.getDisplayName()));
    }

    @Subcommand("deposit")
    @Syntax("<quantia>")
    public void onDeposit(Player player, Double amount) {
        final UUID uuid = player.getUniqueId();
        if (!manager.isMarried(uuid)) {
            plugin.sendMessage(player, Messages.COMMANDS_NOT_MARRIED);
            return;
        }

        if (amount <= 0) {
            plugin.sendMessage(player, Messages.SERVER_NO_MONEY, String.valueOf(amount));
            return;
        }

        if (!plugin.economy.has(player, amount)) {
            plugin.sendMessage(player, Messages.SERVER_NO_MONEY, String.valueOf(amount));
            return;
        }

        manager.giveMarryBankMoney(manager.getMarryId(uuid), amount);
        plugin.economy.withdrawPlayer(player, amount);
        plugin.sendMessage(player, Messages.COMMANDS_DEPOSIT, String.valueOf(amount));
    }

    @Subcommand("balance")
    public void onBalance(Player player) {
        final UUID uuid = player.getUniqueId();
        if (!manager.isMarried(uuid)) {
            plugin.sendMessage(player, Messages.COMMANDS_NOT_MARRIED);
            return;
        }
        plugin.sendMessage(player, Messages.COMMANDS_BALANCE, String.valueOf(plugin.marrieds.get(manager.getMarryId(uuid)).getMarryBalance()));
    }

    @Subcommand("give")
    public void onGiveItem(Player player) {
        final UUID uuid = player.getUniqueId();
        if (!manager.isMarried(uuid)) {
            plugin.sendMessage(player, Messages.COMMANDS_NOT_MARRIED);
            return;
        }

        final Player partner = Bukkit.getPlayer(plugin.marriedUsers.get(uuid).getMarryUUID());
        if (partner == null || !partner.isOnline()) {
            plugin.sendMessage(player, Messages.COMMANDS_OFFLINE);
            return;
        }

        final ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack != air) {
            giveItem(player, partner, itemStack);
        }
    }

    @Subcommand("withdraw")
    @Syntax("<quantia>")
    public void onWithdraw(Player player, Double amount) {
        final UUID uuid = player.getUniqueId();
        if (manager.isMarried(uuid)) {
            if (amount > 0) {
                final int id = manager.getMarryId(uuid);
                if (manager.getMarryMoney(id) >= amount) {
                    manager.removeMarryBankMoney(id, amount);
                    plugin.economy.depositPlayer(player, amount);
                    plugin.sendMessage(player, Messages.COMMANDS_WITHDRAW, String.valueOf(amount));
                } else {
                    plugin.sendMessage(player, Messages.COMMANDS_NOT_MONEY, String.valueOf(amount));
                }
            } else {
                plugin.sendMessage(player, Messages.SERVER_NO_MONEY, "1");
            }
        } else {
            plugin.sendMessage(player, Messages.COMMANDS_NOT_MARRIED);
        }
    }

    @Subcommand("home")
    public void onHome(Player player) {
        final UUID uuid = player.getUniqueId();
        if (manager.isMarried(uuid)) {
            final Location location = plugin.marrieds.get(plugin.marriedUsers.get(uuid).getMarryId()).getMarryLocation();
            if (location != error) {
                
                plugin.teleports.put(player, new PlayerTeleport(plugin, player, location, plugin.getMessage(Messages.COMMANDS_HOME, true)));
            } else {
                plugin.sendMessage(player, Messages.COMMANDS_NO_HOME);
            }
        } else {
            plugin.sendMessage(player, Messages.COMMANDS_NOT_MARRIED);
        }
    }

    @Subcommand("sethome")
    public void onSetHome(Player player) {
        final UUID uuid = player.getUniqueId();
        if (plugin.marriedUsers.containsKey(uuid)) {
            final MarryId marryId = plugin.marrieds.get(plugin.marriedUsers.get(uuid).getMarryId());
            final double setHomeCost = plugin.getDouble(Doubles.SETHOME_COST);
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
                plugin.marrieds.put(id, marryId);

                Update update = new Update(plugin.getString(Strings.TABLE_BANK));
                update.set.set("location", saveloc);
                update.where.set("marry_id", id);
                SQL.executeAsync(update);
                plugin.sendMessage(player, Messages.COMMANDS_HOME_SAVE, String.valueOf(setHomeCost));
            } else {
                plugin.sendMessage(player, Messages.COMMANDS_NOT_MONEY, String.valueOf(marryMoney));
            }
        } else {
            plugin.sendMessage(player, Messages.COMMANDS_NOT_MARRIED);
        }
    }

    private void giveItem(Player player, Player target, ItemStack itemStack) {
        target.getInventory().addItem(itemStack);
        player.getInventory().setItemInMainHand(air);
    }

    private void marrySucess(final PlayerMarryPropose playerMarryPropose) {
        final long time = System.currentTimeMillis();

        Insert insert = new Insert(plugin.getString(Strings.TABLE_BANK));
        insert.columns.set("marry_id", "balance", "hours", "location", "time", "last");
        insert.values.set((plugin.marrieds.size() + 1), 0, 0, "world:666:666:666:666:666", time, time);
        SQL.executeAsync(insert);

        save(playerMarryPropose, plugin.marrieds.size() + 1);
        plugin.marrieds.put(plugin.marrieds.size() + 1, new MarryId(plugin.marrieds.size() + 1, 0.0, 0, error, time, time));
    }

    private void save(final PlayerMarryPropose playerMarryPropose, int id) {

        Insert insert = new Insert(plugin.getString(Strings.TABLE_MARRY));
        insert.columns.set("uuid", "marry_uuid", "marry_name", "marry_display", "marry_id");
        insert.values.set(playerMarryPropose.getWifeUUID().toString(), playerMarryPropose.getHusbandUUID().toString(), playerMarryPropose.getHusbandName(), playerMarryPropose.getHusbandDisplayName(), id);
        SQL.executeAsync(insert);

        insert = new Insert(plugin.getString(Strings.TABLE_MARRY));
        insert.columns.set("uuid", "marry_uuid", "marry_name", "marry_display", "marry_id");
        insert.values.set(playerMarryPropose.getHusbandUUID().toString(), playerMarryPropose.getWifeUUID().toString(), playerMarryPropose.getWifeName(), playerMarryPropose.getWifeDisplayName(), id);
        SQL.executeAsync(insert);

        plugin.userKiss.put(playerMarryPropose.getWifeUUID(), System.currentTimeMillis());
        plugin.userKiss.put(playerMarryPropose.getHusbandUUID(), System.currentTimeMillis());
        plugin.marriedUsers.put(playerMarryPropose.getWifeUUID(), new PlayerMarry(playerMarryPropose.getWifeUUID(), playerMarryPropose.getHusbandUUID(), playerMarryPropose.getHusbandName(), playerMarryPropose.getHusbandDisplayName(), id));
        plugin.marriedUsers.put(playerMarryPropose.getHusbandUUID(), new PlayerMarry(playerMarryPropose.getHusbandUUID(), playerMarryPropose.getWifeUUID(), playerMarryPropose.getWifeName(), playerMarryPropose.getWifeDisplayName(), id));
    }

    private void marryDeny(final UUID wifeUUID, final UUID husbandUUID) {
        final int id = manager.getMarryId(wifeUUID);

        Delete delete = new Delete(plugin.getString(Strings.TABLE_BANK));
        delete.where.set("marry_id", id);
        SQL.executeAsync(delete);

        delete = new Delete(plugin.getString(Strings.TABLE_MARRY));
        delete.where.set("uuid", wifeUUID.toString());
        SQL.executeAsync(delete);

        delete = new Delete(plugin.getString(Strings.TABLE_MARRY));
        delete.where.set("uuid", husbandUUID.toString());
        SQL.executeAsync(delete);

        plugin.marrieds.remove(id);
        plugin.marriedUsers.remove(wifeUUID);
        plugin.marriedUsers.remove(husbandUUID);
    }

}
