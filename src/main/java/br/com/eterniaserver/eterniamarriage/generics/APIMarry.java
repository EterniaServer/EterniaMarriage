package br.com.eterniaserver.eterniamarriage.generics;

import br.com.eterniaserver.eternialib.EQueries;
import br.com.eterniaserver.eternialib.UUIDFetcher;
import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.Strings;
import br.com.eterniaserver.eterniamarriage.objects.MarryId;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class APIMarry {

    private APIMarry() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isMarried(UUID uuid) {
        return Vars.marriedUsers.containsKey(uuid);
    }

    public static UUID getPartnerUUID(UUID uuid) {
        return Vars.marriedUsers.get(uuid).getMarryUUID();
    }

    public static String getPartnerName(UUID uuid) {
        return Vars.marriedUsers.get(uuid).getMarryName();
    }

    public static String getPartnerDisplay(UUID uuid) {
        return Vars.marriedUsers.get(uuid).getMarryDisplay();
    }

    public static boolean isCloseToPartner(OfflinePlayer player) {
        final Location location = player.getPlayer().getLocation();
        final OfflinePlayer partner = Bukkit.getOfflinePlayer(getPartnerUUID(UUIDFetcher.getUUIDOf(player.getName())));
        if (partner.isOnline()) {
            final Location partnerLocation = partner.getPlayer().getLocation();
            return partnerLocation.getWorld() == location.getWorld() && partnerLocation.distanceSquared(location) <= 100;
        }
        return false;
    }

    public static boolean isReallyClose(OfflinePlayer player) {
        final OfflinePlayer partner = Bukkit.getOfflinePlayer(getPartnerUUID(UUIDFetcher.getUUIDOf(player.getName())));
        if (partner.isOnline()) {
            final Location location = player.getPlayer().getLocation();
            final Location partnerLocation = partner.getPlayer().getLocation();
            return partnerLocation.getWorld() == location.getWorld() && partnerLocation.distanceSquared(location) <= 2;
        }
        return false;
    }

    public static int getMarryId(UUID uuid) {
        return Vars.marriedUsers.get(uuid).getMarryId();
    }

    public static double getMarryMoney(int id) {
        return Vars.marrieds.get(id).getMarryBalance();
    }

    public static void setMarryMoney(int id, double amount) {
        final MarryId marryId = Vars.marrieds.get(id);
        marryId.setMarryBalance(amount);
        EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_BANK, Strings.BALANCE, amount, Strings.MARRY_ID, id));
        Vars.marrieds.put(id, marryId);
    }

    public static void giveMarryBankMoney(int id, double amount) {
        setMarryMoney(id, getMarryMoney(id) + amount);
    }

    public static void removeMarryBankMoney(int id, double amount) {
        setMarryMoney(id, getMarryMoney(id) - amount);
    }

}
