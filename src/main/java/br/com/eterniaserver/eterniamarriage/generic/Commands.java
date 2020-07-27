package br.com.eterniaserver.eterniamarriage.generic;

import br.com.eterniaserver.eternialib.EFiles;
import br.com.eterniaserver.eternialib.EQueries;
import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

@CommandAlias("marry")
@CommandPermission("eternia.marry")
public class Commands extends BaseCommand {

    private final EterniaMarriage plugin;
    private final ItemStack air = new ItemStack(Material.AIR);
    private final EFiles messages;
    private final Location error;

    public Commands(EterniaMarriage plugin) {
        this.plugin = plugin;
        this.messages = plugin.getEFiles();
        error = new Location(Bukkit.getWorld("world"), 666, 666, 666, 666, 666);

        String query = "SELECT * FROM " + EterniaMarriage.serverConfig.getString("sql.table-marry") + ";";
        HashMap<String, String> temp = EQueries.getMapString(query, "player_name", "marry_name");
        temp.forEach(Vars.marry::put);
        messages.sendConsole("server.loaded", Constants.MODULE.get(), "Married", Constants.AMOUNT.get(), temp.size() / 2);

        query = "SELECT * FROM " + EterniaMarriage.serverConfig.getString("sql.table-bank") + ";";

        temp = EQueries.getMapString(query, "marry_bank", "balance");
        temp.forEach((k, v) -> Vars.marryBank.put(k, Double.parseDouble(v)));
        final int sized = temp.size();
        messages.sendConsole("server.loaded", Constants.MODULE.get(), "MarryMoney", Constants.AMOUNT.get(), sized);

        temp = EQueries.getMapString(query, "marry_bank", "marry_time");
        temp.forEach((k, v) -> Vars.marryTime.put(k, Long.parseLong(v)));
        messages.sendConsole("server.loaded", Constants.MODULE.get(), "MarryTime", Constants.AMOUNT.get(), sized);

        temp = EQueries.getMapString(query, "marry_bank", "location");
        temp.forEach((k, v) -> {
            final String[] split = v.split(":");
            final Location loc = new Location(Bukkit.getWorld(split[0]),
                    Double.parseDouble(split[1]),
                    (Double.parseDouble(split[2]) + 1),
                    Double.parseDouble(split[3]),
                    Float.parseFloat(split[4]),
                    Float.parseFloat(split[5]));
            Vars.marryLocation.put(k, loc);
        });
        messages.sendConsole("server.loaded", Constants.MODULE.get(), "MarryHomes", Constants.AMOUNT.get(), sized);
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

        if (!EterniaMarriage.econ.has(player, money)) {
            messages.sendMessage("server.no-balance", "%money%", money, player);
            return;
        }

        if (wifeName.equals(husbandName)) {
            messages.sendMessage("server.yourself", player);
            return;
        }
        if (APIMarry.isMarried(wifeName) || APIMarry.isMarried(husbandName)) {
            messages.sendMessage("marry.already-married", player);
            return;
        }
        if ((Vars.proMarry.containsKey(wifeName) || Vars.proMarry.containsKey(husbandName))) {
            messages.sendMessage("marry.already-sent", player);
            return;
        }

        EterniaMarriage.econ.withdrawPlayer(player, money);
        messages.broadcastMessage("marry.advice", Constants.PLAYER.get(), wife.getDisplayName(), Constants.TARGET.get(), husband.getDisplayName());
        sendMarry(wife, husband, wifeName);
        sendMarry(husband, wife, husbandName);
    }

    @Subcommand("reload")
    @CommandPermission("eternia.reload")
    public void onReload(Player player) {
        plugin.getFiles().loadConfigs();
        plugin.getFiles().loadDatabase();
        plugin.getFiles().loadMessages();
        messages.sendMessage("server.reload", player);
    }

    @Subcommand("divorce")
    @Syntax("<jogador(a)> <jogador(a)>")
    @CommandCompletion("@players @players")
    @CommandPermission("eternia.priest")
    public void onDivorce(Player player, String wifeName, String husbandName) {
        if (Vars.marry.get(wifeName).equals(husbandName) && Vars.marry.get(husbandName).equals(wifeName)) {
            marryDeny(wifeName, husbandName);
        } else {
            messages.sendMessage("marry.no-marry", player);
        }
    }

    @Subcommand("accept")
    public void onAccept(Player player) {
        final String wifeName = player.getName();

        if (Vars.proMarry.containsValue(wifeName)) {
            final String husbandName = Vars.proMarry.get(wifeName);
            if (Vars.resMarry.get(husbandName)) {
                messages.broadcastMessage("marry.sucess", Constants.TARGET.get(), husbandName, Constants.PLAYER.get(), player.getName());
                marrySucess(wifeName, husbandName);
                Vars.resMarry.remove(wifeName);
                Vars.resMarry.remove(husbandName);
                Vars.proMarry.remove(wifeName);
                Vars.proMarry.remove(husbandName);
            } else {
                Vars.resMarry.put(wifeName, true);
                messages.broadcastMessage("marry.accept", Constants.PLAYER.get(), player.getDisplayName());
            }
        }else {
            messages.sendMessage("marry.no-proposal", player);
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
            messages.broadcastMessage("marry.deny", Constants.PLAYER.get(), player.getDisplayName());
        } else {
            messages.sendMessage("marry.no-proposal", player);
        }
    }

    @Subcommand("deposit")
    @Syntax("<quantia>")
    public void onDeposit(Player player, Double amount) {
        final String playerName = player.getName();
        final String marryBank = APIMarry.getMarriedBankName(playerName);
        if (!marryBank.equals("")) {
            if (amount > 0) {
                if (EterniaMarriage.econ.has(playerName, amount)) {
                    EterniaMarriage.econ.withdrawPlayer(playerName, amount);
                    APIMarry.giveMarryBankMoney(marryBank, amount);
                    messages.sendMessage("commands.deposit", Constants.AMOUNT.get(), amount, player);
                } else {
                    messages.sendMessage("server.no-bal", player);
                }
            } else {
                messages.sendMessage("server.no-money", player);
            }
        } else {
            messages.sendMessage("commands.no-marry", player);
        }
    }

    @Subcommand("give")
    public void onGiveItem(Player player) {
        if (Vars.marry.containsKey(player.getName())) {
            final Player partner = Bukkit.getPlayer(APIMarry.getPartner(player.getName()));
            if (partner != null && partner.isOnline()) {
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack != air) {
                    giveItem(player, partner, itemStack);
                }
            } else {
                messages.sendMessage("commands.offline", player);
            }
        } else {
            messages.sendMessage("commands.no-marry", player);
        }

    }

    @Subcommand("withdraw")
    @Syntax("<quantia>")
    public void onWithdraw(Player player, Double amount) {
        final String playerName = player.getName();
        final String marryBank = APIMarry.getMarriedBankName(playerName);
        if (!marryBank.equals("")) {
            if (amount > 0) {
                if (Vars.marryBank.get(marryBank) >= amount) {
                    EterniaMarriage.econ.depositPlayer(playerName, amount);
                    APIMarry.removeMarryBankMoney(marryBank, amount);
                } else {
                    messages.sendMessage("commands.no-money", "%money%", amount, player);
                }
            } else {
                messages.sendMessage("server.no-money", player);
            }
        } else {
            messages.sendMessage("commands.no-marry", player);
        }
    }

    @Subcommand("home")
    public void onHome(Player player) {
        final String marryName = APIMarry.getMarriedBankName(player.getName());
        if (Vars.marryLocation.get(marryName) != error) {
            PaperLib.teleportAsync(player, Vars.marryLocation.get(marryName), PlayerTeleportEvent.TeleportCause.PLUGIN);
            messages.sendMessage("commands.home", player);
        } else {
            messages.sendMessage("commands.no-home", player);
        }
    }

    @Subcommand("sethome")
    public void onSetHome(Player player) {
        Location loc = player.getLocation();
        final String marryName = APIMarry.getMarriedBankName(player.getName());
        final double marryMoney = Vars.marryBank.get(marryName);
        final double setHomeCost = EterniaMarriage.serverConfig.getDouble("money.sethome");
        if (marryMoney >= setHomeCost) {
            APIMarry.removeMarryBankMoney(marryName, setHomeCost);
            final String saveloc = loc.getWorld().getName() +
                    ":" + ((int) loc.getX()) +
                    ":" + ((int) loc.getY()) +
                    ":" + ((int) loc.getZ()) +
                    ":" + ((int) loc.getYaw()) +
                    ":" + ((int) loc.getPitch());
            EQueries.executeQuery("UPDATE " + EterniaMarriage.serverConfig.getString("sql.table-bank") + " SET location='" + saveloc + "' WHERE marry_bank='" + marryName + "';");
            Vars.marryLocation.put(marryName, loc);
            messages.sendMessage("commands.home-save", "%money%", setHomeCost, player);
        } else {
            messages.sendMessage("commands.no-money", "%money%", setHomeCost, player);
        }

    }

    private void giveItem(Player player, Player target, ItemStack itemStack) {
        target.getInventory().addItem(itemStack);
        player.getInventory().setItemInMainHand(air);
    }

    private void sendMarry(Player player, Player target, String name) {
        messages.sendMessage("marry.send-proposal", Constants.PLAYER.get(), player.getDisplayName(), Constants.TARGET.get(), target.getDisplayName(), player);
        Vars.resMarry.put(name, false);
        Vars.proMarry.put(name, target.getName());
    }

    private void marrySucess(String nameOne, String nameTwo) {
        EQueries.executeQuery("INSERT INTO " + EterniaMarriage.serverConfig.getString("sql.table-marry") + " (player_name, marry_name) VALUES('" + nameOne + "', '" + nameTwo + "');");
        EQueries.executeQuery("INSERT INTO " + EterniaMarriage.serverConfig.getString("sql.table-marry") + " (player_name, marry_name) VALUES('" + nameTwo + "', '" + nameOne + "');");
        EQueries.executeQuery("INSERT INTO " + EterniaMarriage.serverConfig.getString("sql.table-bank") +
                " (marry_bank, balance, marry_time, location) VALUES('" + nameOne + nameTwo + "', '" + 0 + "', '" + 0 + "', 'world:666:666:666:666:666');");
        Vars.marry.put(nameOne, nameTwo);
        Vars.marry.put(nameTwo, nameOne);
    }

    private void marryDeny(String nameOne, String nameTwo) {
        String nameBank;
        if (Vars.marryBank.containsKey(nameOne + nameTwo)) nameBank = nameOne + nameTwo;
        else nameBank = nameTwo + nameOne;
        EQueries.executeQuery("DELETE FROM " + EterniaMarriage.serverConfig.getString("sql.table-bank") + " WHERE marry_bank='" + nameBank + "'");
        EQueries.executeQuery("DELETE FROM " + EterniaMarriage.serverConfig.getString("sql.table-marry") + " WHERE player_name='" + nameOne + "'");
        EQueries.executeQuery("DELETE FROM " + EterniaMarriage.serverConfig.getString("sql.table-marry") + " WHERE player_name='" + nameTwo + "'");
        Vars.marry.remove(nameOne);
        Vars.marry.remove(nameTwo);
        Vars.marryLocation.remove(nameBank);
        Vars.marryTime.remove(nameBank);
        Vars.marryBank.remove(nameBank);
    }

}
