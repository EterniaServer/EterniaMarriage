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

        HashMap<String, String> temp = EQueries.getMapString(Constants.getQuerySelectAll(Constants.TABLE_MARRY), Strings.PNAME, Strings.MARRY_NAME);
        temp.forEach(Vars.marry::put);
        messages.sendConsole(Strings.M_SERVER_LOAD, Constants.MODULE, "Married", Constants.AMOUNT, temp.size() / 2);

        final String query = Constants.getQuerySelectAll(Constants.TABLE_BANK);
        temp = EQueries.getMapString(query, Strings.MARRY_BANK, Strings.BALANCE);
        temp.forEach((k, v) -> Vars.marryBank.put(k, Double.parseDouble(v)));
        final int sized = temp.size();
        messages.sendConsole(Strings.M_SERVER_LOAD, Constants.MODULE, "Marry Money", Constants.AMOUNT, sized);

        temp = EQueries.getMapString(query, Strings.MARRY_BANK, Strings.MARRY_TIME);
        temp.forEach((k, v) -> Vars.marryTime.put(k, Long.parseLong(v)));
        messages.sendConsole(Strings.M_SERVER_LOAD, Constants.MODULE, "Marry Time", Constants.AMOUNT, sized);

        temp = EQueries.getMapString(query, Strings.MARRY_BANK, Strings.LOC);
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
        messages.sendConsole(Strings.M_SERVER_LOAD, Constants.MODULE, "Marry Homes", Constants.AMOUNT, sized);
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

        if (!economy.has(player, money)) {
            messages.sendMessage(Strings.M_BALANCE_NO, Constants.MONEY, money, player);
            return;
        }

        if (wifeName.equals(husbandName)) {
            messages.sendMessage(Strings.M_SERVER_YOUR, player);
            return;
        }
        if (APIMarry.isMarried(wifeName) || APIMarry.isMarried(husbandName)) {
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
        if (Vars.marry.get(wifeName).equals(husbandName) && Vars.marry.get(husbandName).equals(wifeName)) {
            marryDeny(wifeName, husbandName);
        } else {
            messages.sendMessage(Strings.M_MARRY_NO, player);
        }
    }

    @Subcommand("accept")
    public void onAccept(Player player) {
        final String wifeName = player.getName();
        if (Vars.proMarry.containsValue(wifeName)) {
            final String husbandName = Vars.proMarry.get(wifeName);
            if (Vars.resMarry.get(husbandName)) {
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
        final String marryBank = APIMarry.getMarriedBankName(playerName);
        if (!marryBank.equals("")) {
            if (amount > 0) {
                if (economy.has(player, amount)) {
                    economy.withdrawPlayer(player, amount);
                    APIMarry.giveMarryBankMoney(marryBank, amount);
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
        if (Vars.marry.containsKey(player.getName())) {
            final Player partner = Bukkit.getPlayer(APIMarry.getPartner(player.getName()));
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
        final String marryBank = APIMarry.getMarriedBankName(playerName);
        if (!marryBank.equals("")) {
            if (amount > 0) {
                if (Vars.marryBank.get(marryBank) >= amount) {
                    economy.depositPlayer(player, amount);
                    APIMarry.removeMarryBankMoney(marryBank, amount);
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
        final String marryName = APIMarry.getMarriedBankName(player.getName());
        if (Vars.marryLocation.get(marryName) != error) {
            Vars.teleports.put(player, new PlayerTeleport(player, Vars.marryLocation.get(marryName), Strings.M_COMMANDS_DONE));
        } else {
            messages.sendMessage(Strings.M_COMMANDS_NO_HOME, player);
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
            EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_BANK, Strings.LOC, saveloc, Strings.MARRY_BANK, marryName));
            Vars.marryLocation.put(marryName, loc);
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
        EQueries.executeQuery(Constants.getQueryInsert(Constants.TABLE_MARRY, Strings.PNAME, nameOne, Strings.MARRY_NAME, nameTwo));
        EQueries.executeQuery(Constants.getQueryInsert(Constants.TABLE_MARRY, Strings.PNAME, nameTwo, Strings.MARRY_NAME, nameOne));
        EQueries.executeQuery(Constants.getQueryInsert(Constants.TABLE_BANK, Strings.MARRY_BANK, nameOne + nameTwo,
                Strings.BALANCE, 0, Strings.MARRY_TIME, 0, Strings.LOC, "world:666:666:666:666:666"));
        Vars.marry.put(nameOne, nameTwo);
        Vars.marry.put(nameTwo, nameOne);
    }

    private void marryDeny(String nameOne, String nameTwo) {
        String nameBank;
        if (Vars.marryBank.containsKey(nameOne + nameTwo)) nameBank = nameOne + nameTwo;
        else nameBank = nameTwo + nameOne;
        EQueries.executeQuery(Constants.getQueryDelete(Constants.TABLE_BANK, Strings.MARRY_BANK, nameBank));
        EQueries.executeQuery(Constants.getQueryDelete(Constants.TABLE_MARRY, Strings.PNAME, nameOne));
        EQueries.executeQuery(Constants.getQueryDelete(Constants.TABLE_MARRY, Strings.PNAME, nameTwo));
        Vars.marry.remove(nameOne);
        Vars.marry.remove(nameTwo);
        Vars.marryLocation.remove(nameBank);
        Vars.marryTime.remove(nameBank);
        Vars.marryBank.remove(nameBank);
    }

}
