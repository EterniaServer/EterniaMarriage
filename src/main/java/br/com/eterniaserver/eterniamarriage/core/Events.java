package br.com.eterniaserver.eterniamarriage.core;

import br.com.eterniaserver.eternialib.SQL;
import br.com.eterniaserver.eternialib.sql.queries.Update;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.enums.Strings;
import br.com.eterniaserver.eterniamarriage.objects.MarryId;
import br.com.eterniaserver.eterniamarriage.objects.PlayerMarry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Events implements Listener {

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final String playerName = player.getName();
        final String playerDisplay = player.getDisplayName();
        final UUID uuid = player.getUniqueId();
        final long time = System.currentTimeMillis();

        if (!Vars.marriedUsers.containsKey(uuid)) return;

        final PlayerMarry playerMarry = Vars.marriedUsers.get(Vars.marriedUsers.get(uuid).getMarryUUID());
        final Player partner = Bukkit.getPlayer(playerMarry.getUuid());

        if (!playerMarry.getMarryName().equals(playerName)) {
            playerMarry.setMarryName(playerName);
            Update update = new Update(EterniaMarriage.getString(Strings.TABLE_MARRY));
            update.set.set("marry_name", playerName);
            update.where.set("uuid", playerMarry.getUuid());
            SQL.executeAsync(update);
        }
        if (!playerMarry.getMarryDisplay().equals(playerDisplay)) {
            playerMarry.setMarryDisplay(playerDisplay);
            Update update = new Update(EterniaMarriage.getString(Strings.TABLE_MARRY));
            update.set.set("marry_display", playerDisplay);
            update.where.set("uuid", playerMarry.getUuid());
            SQL.executeAsync(update);
        }


        if (partner != null) {
            final int id = playerMarry.getMarryId();
            final MarryId marryId = Vars.marrieds.get(id);
            marryId.setMarryLast(time);
            
            if (Vars.marryOnline.getOrDefault(id, false).equals(Boolean.TRUE)) {
                updateTime(id);
            }

            Update update = new Update(EterniaMarriage.getString(Strings.TABLE_BANK));
            update.set.set("last", time);
            update.where.set("marry_id", id);
            SQL.executeAsync(update);
            Vars.marrieds.put(id, marryId);
            Vars.marryOnline.put(id, true);
        }

        Vars.marriedUsers.put(playerMarry.getUuid(), playerMarry);
        Vars.userKiss.put(uuid, time);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        if (!Vars.marriedUsers.containsKey(uuid)) return;

        final PlayerMarry playerMarry = Vars.marriedUsers.get(uuid);
        final Player partner = Bukkit.getPlayer(playerMarry.getMarryUUID());

        if (partner != null) {
            final int id = playerMarry.getMarryId();
            updateTime(id);
            Vars.marryOnline.put(id, false);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        if (!(event.getRightClicked() instanceof Player)) return;
        final Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        final UUID uuid = player.getUniqueId();
        if (!Vars.userKiss.containsKey(uuid)) {
            Vars.userKiss.put(uuid, System.currentTimeMillis());
        }
        if (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - Vars.userKiss.get(uuid)) < 30) return;
        if (!APIMarry.isMarried(uuid)) return;
        if (!APIMarry.isReallyClose(player)) return;

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

    private void updateTime(int id) {
        final MarryId marryId = Vars.marrieds.get(id);
        int hours = marryId.getMarryHours() + (int) TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - marryId.getMarryLast());
        marryId.setMarryHours(hours);

        Update update = new Update(EterniaMarriage.getString(Strings.TABLE_BANK));
        update.set.set("hours", hours);
        update.where.set("marry_id", id);
        SQL.executeAsync(update);
        Vars.marrieds.put(id, marryId);
    }

}
