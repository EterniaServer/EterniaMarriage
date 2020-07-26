package br.com.eterniaserver.eterniamarriage.generic;

import br.com.eterniaserver.eternialib.EFiles;
import br.com.eterniaserver.eternialib.EQueries;
import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

@CommandAlias("casar|marry")
@CommandPermission("eternia.marry")
public class Commands extends BaseCommand {

    private final ItemStack air = new ItemStack(Material.AIR);
    private final EFiles messages;

    public Commands(EterniaMarriage plugin) {
        this.messages = plugin.getEFiles();

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
        final Player pMarryOne = marryOne.getPlayer();
        final Player pMarryTwo = marryTwo.getPlayer();
        final String marryOneN = pMarryOne.getName();
        final String marryTwoN = pMarryTwo.getName();
        if (pMarryOne == pMarryTwo) {
            messages.sendMessage("server.yourself", player);
            return;
        }
        if (APIMarry.isMarried(marryOneN) || APIMarry.isMarried(marryTwoN)) {
            messages.sendMessage("marry.already-married", player);
            return;
        }
        if ((Vars.proMarry.containsKey(marryOneN) && Vars.proMarry.get(marryOneN).equals(marryTwoN)) &&
                (Vars.proMarry.containsKey(marryTwoN) && Vars.proMarry.get(marryTwoN).equals(marryOneN))) {
            messages.sendMessage("marry.already-sent", player);
            return;
        }
        if (Vars.resMarry.get(marryOneN) && Vars.resMarry.get(marryTwoN)) {
            Vars.resMarry.remove(marryOneN);
            Vars.resMarry.remove(marryTwoN);
            Vars.proMarry.remove(marryOneN);
            Vars.proMarry.remove(marryTwoN);
            messages.broadcastMessage("marry.sucess", Constants.PLAYER.get(), player.getDisplayName(), Constants.TARGET.get(), pMarryOne.getDisplayName(), "%target2_displayname%", pMarryTwo.getDisplayName());
            marrySucess(marryOneN, marryTwoN);
            return;
        }
        if (!Vars.proMarry.containsKey(marryOneN) && !Vars.proMarry.containsKey(marryTwoN)) {
            messages.broadcastMessage("marry.advice", Constants.PLAYER.get(), pMarryOne.getDisplayName(), Constants.TARGET.get(), pMarryTwo.getDisplayName());
            sendMarry(pMarryOne, pMarryTwo, marryOneN);
            sendMarry(pMarryTwo, pMarryOne, marryTwoN);
        } else {
            messages.sendMessage("marry.already-proposal", player);
        }
    }

    @CommandAlias("divorciar|divorce")
    @Syntax("<jogador(a)> <jogador(a)>")
    @CommandCompletion("@players @players")
    @CommandPermission("eternia.priest")
    public void onDivorce(Player player, String marryOne, String marryTwo) {
        if (Vars.marry.get(marryOne).equals(marryTwo) && Vars.marry.get(marryTwo).equals(marryOne)) {
            marryDeny(marryOne, marryTwo);
        } else {
            messages.sendMessage("marry.no-marry", player);
        }
    }

    @Subcommand("accept|aceitar")
    public void onAccept(Player player) {
        final String name = player.getName();
        if (Vars.proMarry.containsValue(name)) {
            Vars.resMarry.put(name, true);
            Vars.proMarry.remove(name);
            messages.broadcastMessage("marry.accept", Constants.PLAYER.get(), player.getDisplayName());
        } else {
            messages.sendMessage("marry.no-proposal", player);
        }
    }

    @Subcommand("deny|negar")
    public void onDeny(Player player) {
        final String name = player.getName();
        if (Vars.proMarry.containsValue(name)) {
            final String targetName = Vars.proMarry.get(name);
            Vars.resMarry.remove(targetName);
            Vars.proMarry.remove(targetName);
            Vars.resMarry.remove(name);
            Vars.proMarry.remove(name);
            messages.broadcastMessage("marry.deny", Constants.PLAYER.get(), player.getDisplayName());
        } else {
            messages.sendMessage("marry.no-proposal", player);
        }
    }

    @Subcommand("deposit|depositar")
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

    @Subcommand("dar|give")
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

    @Subcommand("withdraw|sacar")
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
                    messages.sendMessage("commands.no-money", player);
                }
            } else {
                messages.sendMessage("server.no-money", player);
            }
        } else {
            messages.sendMessage("commands.no-marry", player);
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
