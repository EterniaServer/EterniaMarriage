package br.com.eterniaserver.eterniamarriage.generic;

import br.com.eterniaserver.eternialib.EQueries;
import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.Strings;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class APIMarry {

    private APIMarry() {
        throw new IllegalStateException("Utility class");
    }
    public static boolean isMarried(UUID uuid) {
        return Vars.marryId.containsKey(uuid);
    }

    public static UUID getPartnerUUID(UUID uuid) {
        return Vars.userMarry.get(uuid);
    }

    public static String getPartnerName(UUID uuid) {
        return Vars.marryName.get(getPartnerUUID(uuid));
    }

    public static String getPartnerDisplay(UUID uuid) {
        return Vars.marryDisplay.get(uuid);
    }

    public static boolean isCloseToPartner(Player player) {
        final Location location = player.getLocation();
        final OfflinePlayer partner = Bukkit.getOfflinePlayer(getPartnerUUID(UUIDFetcher.getUUIDOf(player.getName())));
        if (partner.isOnline()) {
            Location partnerLocation = partner.getPlayer().getLocation();
            return partnerLocation.getWorld() == location.getWorld() && partnerLocation.distanceSquared(location) <= 100;
        }
        return false;
    }

    public static int getMarryBankId(UUID uuid) {
        return Vars.marryId.get(uuid);
    }

    public static double getMarryBankMoney(int bankId) {
        return Vars.marryBankMoney.getOrDefault(bankId, 0.0);
    }

    public static void setMarryBankMoney(int id, double amount) {
        if (Vars.marryBankMoney.containsKey(id)) {
            Vars.marryBankMoney.put(id, amount);
            EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_BANK, Strings.BALANCE, amount, Strings.MARRY_ID, id));
        }
    }

    public static void giveMarryBankMoney(int id, double amount) {
        setMarryBankMoney(id, getMarryBankMoney(id) + amount);
    }

    public static void removeMarryBankMoney(int id, double amount) {
        setMarryBankMoney(id, getMarryBankMoney(id) - amount);
    }

}
