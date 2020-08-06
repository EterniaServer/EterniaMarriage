package br.com.eterniaserver.eterniamarriage.generic;

import br.com.eterniaserver.acf.BaseCommand;
import br.com.eterniaserver.acf.annotation.*;
import br.com.eterniaserver.acf.bukkit.contexts.OnlinePlayer;
import br.com.eterniaserver.eternialib.EFiles;
import br.com.eterniaserver.eternialib.EQueries;
import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;

import br.com.eterniaserver.eterniamarriage.Strings;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandAlias("marry")
@CommandPermission("eternia.marry")
public class Commands extends BaseCommand {

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

        String query = Constants.getQuerySelectAll(Constants.TABLE_MARRY);
        HashMap<String, String> temp = EQueries.getMapString(query, Strings.UUID, Strings.MARRY_UUID);
        temp.forEach((k, v) -> Vars.userMarry.put(UUID.fromString(k), UUID.fromString(v)));

        temp = EQueries.getMapString(query, Strings.MARRY_UUID, Strings.MARRY_NAME);
        temp.forEach((k, v) -> {
            UUID uuid = UUID.fromString(k);
            Vars.marryName.put(Vars.userMarry.get(uuid), v);
        });

        temp = EQueries.getMapString(query, Strings.MARRY_UUID, Strings.MARRY_DISPLAY);
        temp.forEach((k, v) -> Vars.marryDisplay.put(Vars.userMarry.get(UUID.fromString(k)), v));

        temp = EQueries.getMapString(query, Strings.UUID, Strings.MARRY_ID);
        temp.forEach((k, v) -> Vars.marryId.put(UUID.fromString(k), Integer.parseInt(v)));
        messages.sendConsole(Strings.M_SERVER_LOAD, Constants.MODULE, "Marrieds", Constants.AMOUNT, temp.size() / 2);

        query = Constants.getQuerySelectAll(Constants.TABLE_CACHE);
        temp = EQueries.getMapString(query, Strings.UUID, Strings.PNAME);
        temp.forEach((k, v) -> {
            UUID uuid = UUID.fromString(k);
            UUIDFetcher.lookupCache.put(v, uuid);
            Vars.userCache.put(uuid, v);
        });
        messages.sendConsole(Strings.M_SERVER_LOAD, Constants.MODULE, "Marry Cache", Constants.AMOUNT, temp.size());

        query = Constants.getQuerySelectAll(Constants.TABLE_BANK);
        temp = EQueries.getMapString(query, Strings.MARRY_ID, Strings.BALANCE);
        temp.forEach((k, v) -> Vars.marryBankMoney.put(Integer.parseInt(k), Double.parseDouble(v)));

        temp = EQueries.getMapString(query, Strings.MARRY_ID, Strings.HOURS);
        temp.forEach((k, v) -> Vars.marryHours.put(Integer.parseInt(k), Integer.parseInt(v)));

        temp = EQueries.getMapString(query, Strings.MARRY_ID, Strings.LOC);
        temp.forEach((k, v) -> {
            final String[] split = v.split(":");
            final Location loc = new Location(Bukkit.getWorld(split[0]),
                    Double.parseDouble(split[1]),
                    (Double.parseDouble(split[2]) + 1),
                    Double.parseDouble(split[3]),
                    Float.parseFloat(split[4]),
                    Float.parseFloat(split[5]));
            Vars.marryLocation.put(Integer.parseInt(k), loc);
        });

        temp = EQueries.getMapString(query, Strings.MARRY_ID, Strings.TIME);
        temp.forEach((k, v) -> Vars.marryMade.put(Integer.parseInt(k), Long.parseLong(v)));

        temp = EQueries.getMapString(query, Strings.MARRY_ID, Strings.LAST);
        temp.forEach((k, v) -> {
            Vars.marryOnline.put(Integer.parseInt(k), false);
            Vars.marryLastSee.put(Integer.parseInt(k), Long.parseLong(v));
        });

        Vars.marryIdList = temp.size();
        messages.sendConsole(Strings.M_SERVER_LOAD, Constants.MODULE, "Marry Accounts", Constants.AMOUNT, Vars.marryIdList);
    }

    @Default
    @Syntax("<jogador(a)> <jogador(a)>")
    @CommandCompletion("@players @players")
    @CommandPermission("eternia.priest")
    public void onMarry(Player player, OnlinePlayer marryOne, OnlinePlayer marryTwo) {
        final Player wife = marryOne.getPlayer();
        final Player husband = marryTwo.getPlayer();

        final String wifeName = wife.getName();
        final String husbandName = husband.getName();

        final double money = EterniaMarriage.serverConfig.getDouble("money.marry");
        final UUID uuidWife = UUIDFetcher.getUUIDOf(wifeName);
        final UUID uuidHusband = UUIDFetcher.getUUIDOf(husbandName);
        if (!economy.has(player, money)) {
            messages.sendMessage(Strings.M_BALANCE_NO, Constants.MONEY, money, player);
            return;
        }
        if (wifeName.equals(husbandName)) {
            messages.sendMessage(Strings.M_SERVER_YOUR, player);
            return;
        }
        if (APIMarry.isMarried(uuidWife) || APIMarry.isMarried(uuidHusband)) {
            messages.sendMessage(Strings.M_MARRY_ALREADY, player);
            return;
        }
        if ((Vars.proMarry.containsKey(wifeName) || Vars.proMarry.containsKey(husbandName))) {
            messages.sendMessage(Strings.M_MARRY_ALREADY_SENT, player);
            return;
        }
        economy.withdrawPlayer(player, money);
        messages.broadcastMessage(Strings.M_MARRY_ADVICE, Constants.PLAYER, wife.getDisplayName(), Constants.TARGET, husband.getDisplayName());
        sendMarry(wife, husband, wifeName);
        sendMarry(husband, wife, husbandName);
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
            UUID wifeUUID = UUIDFetcher.getUUIDOf(wifeName);
            UUID husbandUUID = UUIDFetcher.getUUIDOf(husbandName);
            if (Vars.userMarry.containsKey(wifeUUID) && Vars.userMarry.get(wifeUUID) == husbandUUID) {
                marryDeny(wifeUUID, husbandUUID);
            } else {
                messages.sendMessage(Strings.M_MARRY_NO, player);
            }
        });
    }

    @Subcommand("accept")
    public void onAccept(Player player) {
        final String wifeName = player.getName();
        if (Vars.proMarry.containsValue(wifeName)) {
            final String husbandName = Vars.proMarry.get(wifeName);
            if (Boolean.TRUE.equals(Vars.resMarry.get(husbandName))) {
                messages.broadcastMessage(Strings.M_MARRY_SUCESS, Constants.TARGET, husbandName, Constants.PLAYER, player.getName());
                marrySucess(wifeName, husbandName);
                Vars.resMarry.remove(wifeName);
                Vars.resMarry.remove(husbandName);
                Vars.proMarry.remove(wifeName);
                Vars.proMarry.remove(husbandName);
            } else {
                Vars.resMarry.put(wifeName, true);
                messages.broadcastMessage(Strings.M_MARRY_ACCEPT, Constants.PLAYER, player.getDisplayName());
            }
        }else {
            messages.sendMessage(Strings.M_MARRY_PROPOSAL, player);
        }
    }

    @Subcommand("deny")
    public void onDeny(Player player) {
        final String wifeName = player.getName();
        if (Vars.proMarry.containsValue(wifeName)) {
            final String husbandName = Vars.proMarry.get(wifeName);
            Vars.resMarry.remove(husbandName);
            Vars.proMarry.remove(husbandName);
            Vars.resMarry.remove(wifeName);
            Vars.proMarry.remove(wifeName);
            messages.broadcastMessage(Strings.M_MARRY_DENY, Constants.PLAYER, player.getDisplayName());
        } else {
            messages.sendMessage(Strings.M_MARRY_PROPOSAL, player);
        }
    }

    @Subcommand("deposit")
    @Syntax("<quantia>")
    public void onDeposit(Player player, Double amount) {
        final String playerName = player.getName();
        final UUID uuid = UUIDFetcher.getUUIDOf(playerName);
        if (APIMarry.isMarried(uuid)) {
            if (amount > 0) {
                if (economy.has(player, amount)) {
                    economy.withdrawPlayer(player, amount);
                    APIMarry.giveMarryBankMoney(APIMarry.getMarryBankId(uuid), amount);
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
        final String playerName = player.getName();
        final UUID uuid = UUIDFetcher.getUUIDOf(playerName);
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
        final String playerName = player.getName();
        final UUID uuid = UUIDFetcher.getUUIDOf(playerName);
        if (APIMarry.isMarried(uuid)) {
            if (amount > 0) {
                final int id = APIMarry.getMarryBankId(uuid);
                if (APIMarry.getMarryBankMoney(id) >= amount) {
                    economy.depositPlayer(player, amount);
                    APIMarry.removeMarryBankMoney(id, amount);
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
        final String playerName = player.getName();
        final UUID uuid = UUIDFetcher.getUUIDOf(playerName);
        Location location = Vars.marryLocation.getOrDefault(Vars.marryId.get(uuid), error);
        if (location != error) {
            Vars.teleports.put(player, new PlayerTeleport(player, location, Strings.M_COMMANDS_DONE));
        } else {
            messages.sendMessage(Strings.M_COMMANDS_NO_HOME, player);
        }
    }

    @Subcommand("sethome")
    public void onSetHome(Player player) {
        final Location loc = player.getLocation();
        final String saveloc = loc.getWorld().getName() +
                ":" + ((int) loc.getX()) +
                ":" + ((int) loc.getY()) +
                ":" + ((int) loc.getZ()) +
                ":" + ((int) loc.getYaw()) +
                ":" + ((int) loc.getPitch());
        final double setHomeCost = EterniaMarriage.serverConfig.getDouble("money.sethome");
        final int id = Vars.marryId.get(UUIDFetcher.getUUIDOf(player.getName()));
        final double marryMoney = Vars.marryBankMoney.get(id);
        if (marryMoney >= setHomeCost) {
            APIMarry.removeMarryBankMoney(id, setHomeCost);
            EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_BANK, Strings.LOC, saveloc, Strings.MARRY_ID, id), false);
            Vars.marryLocation.put(id, loc);
            messages.sendMessage(Strings.M_COMMANDS_HOME_SAVE, Constants.MONEY, setHomeCost, player);
        } else {
            messages.sendMessage(Strings.M_COMMANDS_NO_MONEY, Constants.MONEY, setHomeCost, player);
        }
    }

    private void giveItem(Player player, Player target, ItemStack itemStack) {
        target.getInventory().addItem(itemStack);
        player.getInventory().setItemInMainHand(air);
    }

    private void sendMarry(Player player, Player target, String name) {
        messages.sendMessage(Strings.M_MARRY_SENT, Constants.PLAYER, player.getDisplayName(), Constants.TARGET, target.getDisplayName(), player);
        Vars.resMarry.put(name, false);
        Vars.proMarry.put(name, target.getName());
    }

    private void marrySucess(String nameOne, String nameTwo) {
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

        Vars.marryMade.put(id, time);
        Vars.marryLastSee.put(id, time);
        Vars.marryBankMoney.put(id, 0.0);
        Vars.marryHours.put(id, 0);

        save(nameOne, nameTwo, Vars.marryIdList);
        save(nameTwo, nameOne, Vars.marryIdList);
    }

    private void save(final String wife, final String husband, int id) {
        UUID wifeUUID = UUIDFetcher.getUUIDOf(wife);
        UUID husbandUUID = UUIDFetcher.getUUIDOf(husband);
        EQueries.executeQuery(Constants.getQueryInsert(Constants.TABLE_MARRY, "(uuid, marry_uuid, marry_name, marry_display, marry_id)",
                "('" + wifeUUID.toString() + "', '" + husbandUUID.toString() + "', '" + husband + "', '" + husband + "', '" + id + "')"), false);
        Vars.marryId.put(wifeUUID, id);
        Vars.userMarry.put(wifeUUID, husbandUUID);
        Vars.marryName.put(wifeUUID, wife);
        Vars.marryDisplay.put(wifeUUID, wife);
    }

    private void marryDeny(UUID wifeUUID, UUID husbandUUID) {
        final int id = APIMarry.getMarryBankId(wifeUUID);
        EQueries.executeQuery(Constants.getQueryDelete(Constants.TABLE_BANK, Strings.MARRY_ID, String.valueOf(id)));
        EQueries.executeQuery(Constants.getQueryDelete(Constants.TABLE_MARRY, Strings.UUID, wifeUUID.toString()));
        EQueries.executeQuery(Constants.getQueryDelete(Constants.TABLE_MARRY, Strings.UUID, husbandUUID.toString()));

        divorceHash(wifeUUID);
        divorceHash(husbandUUID);

        Vars.marryMade.remove(id);
        Vars.marryLastSee.remove(id);
        Vars.marryLocation.remove(id);
        Vars.marryBankMoney.remove(id);
        Vars.marryHours.remove(id);
    }

    private void divorceHash(UUID uuid) {
        Vars.marryId.remove(uuid);
        Vars.userMarry.remove(uuid);
        Vars.marryName.remove(uuid);
        Vars.marryDisplay.remove(uuid);
    }

}
