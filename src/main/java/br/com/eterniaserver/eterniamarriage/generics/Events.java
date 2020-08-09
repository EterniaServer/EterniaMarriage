package br.com.eterniaserver.eterniamarriage.generics;

import br.com.eterniaserver.eternialib.EQueries;
import br.com.eterniaserver.eternialib.UUIDFetcher;
import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.Strings;
import br.com.eterniaserver.eterniamarriage.objects.MarryId;
import br.com.eterniaserver.eterniamarriage.objects.PlayerMarry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Events implements Listener {

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final String playerName = player.getName();
        final String playerDisplay = player.getDisplayName();
        final UUID uuid = UUIDFetcher.getUUIDOf(playerName);
        final long time = System.currentTimeMillis();

        if (Vars.marriedUsers.containsKey(uuid)) {
            final PlayerMarry playerMarry = Vars.marriedUsers.get(Vars.marriedUsers.get(uuid).getMarryUUID());
            if (!playerMarry.getMarryName().equals(playerName)) {
                playerMarry.setMarryName(playerName);
                EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_MARRY, Strings.MARRY_NAME, playerName, Strings.UUID, playerMarry.getUuid()));
            }
            if (!playerMarry.getMarryDisplay().equals(playerDisplay)) {
                playerMarry.setMarryDisplay(playerDisplay);
                EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_MARRY, Strings.MARRY_DISPLAY, playerDisplay, Strings.UUID, playerMarry.getUuid()));
            }
            final Player partner = Bukkit.getOfflinePlayer(playerMarry.getUuid()).getPlayer();
            if (partner != null && partner.isOnline()) {
                final int id = playerMarry.getMarryId();
                final MarryId marryId = Vars.marrieds.get(id);
                marryId.setMarryLast(time);
                if (Vars.marryOnline.getOrDefault(id, false)) {
                    updateTime(id);
                }
                EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_BANK, Strings.LAST, time, Strings.MARRY_ID, id));
                Vars.marrieds.put(id, marryId);
                Vars.marryOnline.put(id, true);
            }
            Vars.marriedUsers.put(playerMarry.getUuid(), playerMarry);
        }
        Vars.userKiss.put(uuid, time);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = UUIDFetcher.getUUIDOf(player.getName());
        if (Vars.marriedUsers.containsKey(uuid)) {
            final PlayerMarry playerMarry = Vars.marriedUsers.get(uuid);
            final Player partner = Bukkit.getOfflinePlayer(playerMarry.getMarryUUID()).getPlayer();
            if (partner != null && partner.isOnline()) {
                final int id = playerMarry.getMarryId();
                updateTime(id);
                Vars.marryOnline.put(id, false);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            final Player player = event.getPlayer();
            if (player.isSneaking()) {
                final UUID uuid = UUIDFetcher.getUUIDOf(player.getName());
                if (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - Vars.userKiss.get(uuid)) >= 30) {
                    if (APIMarry.isMarried(uuid) && APIMarry.isReallyClose(player)) {
                        Location loc = player.getLocation();
                        for (double angle = 0; angle < 2 * Math.PI; angle += 0.2) {
                            final double x = 2 * Math.cos(angle);
                            final double z = 2 * Math.sin(angle);
                            loc.add(x, 1, z);
                            loc.getWorld().spawnParticle(Particle.HEART, loc, 1);
                            loc.subtract(x, 1, z);
                        }
                        Vars.userKiss.put(uuid, System.currentTimeMillis());
                    }
                }
            }
        }
    }

    private void updateTime(int id) {
        final MarryId marryId = Vars.marrieds.get(id);
        int hours = marryId.getMarryHours() + (int) TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - marryId.getMarryLast());
        marryId.setMarryHours(hours);
        EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_BANK, Strings.HOURS, hours, Strings.MARRY_ID, id));
        Vars.marrieds.put(id, marryId);
    }

}
