package br.com.eterniaserver.eterniamarriage.generic;

import br.com.eterniaserver.eternialib.EQueries;
import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.Strings;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Events implements Listener {

    @EventHandler (priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        final String playerName = event.getName();
        final UUID uuid = UUIDFetcher.getUUIDOf(playerName);
        if (!Vars.userCache.containsKey(uuid)) {
            EQueries.executeQuery(Constants.getQueryInsert(Constants.TABLE_CACHE, "(uuid, player_name)", "('" + uuid.toString() + "', '" + playerName + "')"));
        } else {
            if (!Vars.userCache.get(uuid).equals(playerName)) {
                Vars.userCache.put(uuid, playerName);
                EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_CACHE, Strings.PNAME, playerName, Strings.UUID, uuid));
            }
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final String playerName = player.getName();
        final String playerDisplay = player.getDisplayName();
        final UUID uuid = UUIDFetcher.getUUIDOf(playerName);
        if (APIMarry.isMarried(uuid)) {
            final UUID partnerUUID = APIMarry.getPartnerUUID(uuid);
            if (!Vars.marryName.get(partnerUUID).equals(playerName)) {
                Vars.marryName.put(partnerUUID, playerName);
                EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_MARRY, Strings.MARRY_NAME, playerName, Strings.UUID, partnerUUID));
            }
            if (!Vars.marryDisplay.get(partnerUUID).equals(playerDisplay)) {
                Vars.marryDisplay.put(partnerUUID, playerName);
                EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_MARRY, Strings.MARRY_DISPLAY, playerName, Strings.UUID, partnerUUID));
            }
            final Player partner = Bukkit.getPlayer(partnerUUID);
            if (partner != null && partner.isOnline()) {
                int id = APIMarry.getMarryBankId(uuid);
                long time = System.currentTimeMillis();
                EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_BANK, Strings.LAST, time, Strings.MARRY_ID, id));
                if (Vars.marryOnline.get(id)) {
                    updateTime(id);
                }
                Vars.marryLastSee.put(id, time);
                Vars.marryOnline.put(id, true);
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final String playerName = player.getName();
        CompletableFuture.runAsync(() -> {
            final UUID uuid = UUIDFetcher.getUUIDOf(playerName);
            if (APIMarry.isMarried(uuid)) {
                final Player partner = Bukkit.getPlayer(APIMarry.getPartnerUUID(uuid));
                if (partner != null && partner.isOnline()) {
                    int id = APIMarry.getMarryBankId(uuid);
                    updateTime(id);
                    Vars.marryOnline.put(id, false);
                }
            }
        });
    }

    private void updateTime(int id) {
        int hours = Vars.marryHours.get(id);
        hours = hours + (int) TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - Vars.marryLastSee.get(id));
        EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_BANK, Strings.HOURS, hours, Strings.MARRY_ID, id));
        Vars.marryHours.put(id, hours);
    }

}
