package br.com.eterniaserver.eterniamarriage.generic;

import br.com.eterniaserver.eternialib.EQueries;
import br.com.eterniaserver.eterniamarriage.Constants;

import br.com.eterniaserver.eterniamarriage.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class APIMarry {

    private APIMarry() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isMarried(String playerName) {
        return Vars.marry.containsKey(playerName);
    }

    public static String getPartner(String playerName) {
        return Vars.marry.get(playerName);
    }

    public static boolean isCloseToPartner(Player player) {
        Location location = player.getLocation();
        final String playerName = player.getName();
        if (APIMarry.isMarried(playerName)) {
            final String partnerName = APIMarry.getPartner(playerName);
            final Player partner = Bukkit.getPlayer(partnerName);
            if (partner != null && partner.isOnline()) {
                Location partnerLocation = partner.getLocation();
                return partnerLocation.getWorld() == location.getWorld() && partnerLocation.distanceSquared(location) <= 100;
            }
        }
        return false;
    }

    public static String getMarriedBankName(String playerName) {
        if (isMarried(playerName)) {
            final String tempBankName = playerName + Vars.marry.get(playerName);
            return Vars.marryBank.containsKey(tempBankName) ? tempBankName : Vars.marry.get(playerName) + playerName;
        } else {
            return "";
        }
    }

    public static double getMarryBankMoney(String bankName) {
        return Vars.marryBank.containsKey(bankName) ? Vars.marryBank.get(bankName) : 0;
    }

    public static void setMarryBankMoney(String bankName, Double amount) {
        if (Vars.marryBank.containsKey(bankName)) {
            Vars.marryBank.put(bankName, amount);
            EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_BANK, Strings.BALANCE, amount, Strings.MARRY_BANK, bankName));
        }
    }

    public static void giveMarryBankMoney(String bankName, Double amount) {
        setMarryBankMoney(bankName, getMarryBankMoney(bankName) + amount);
    }

    public static void removeMarryBankMoney(String bankName, Double amount) {
        setMarryBankMoney(bankName, getMarryBankMoney(bankName) - amount);
    }

}
