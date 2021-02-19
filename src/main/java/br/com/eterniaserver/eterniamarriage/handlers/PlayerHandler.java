package br.com.eterniaserver.eterniamarriage.handlers;

import br.com.eterniaserver.eternialib.SQL;
import br.com.eterniaserver.eternialib.core.queries.Update;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.core.Manager;
import br.com.eterniaserver.eterniamarriage.core.enums.Strings;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.MarryId;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.PlayerMarry;

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

public class PlayerHandler implements Listener {

    private final EterniaMarriage plugin;
    private final Manager manager;

    public PlayerHandler(final EterniaMarriage plugin, final Manager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final String playerName = player.getName();
        final String playerDisplay = player.getDisplayName();
        final UUID uuid = player.getUniqueId();
        final long time = System.currentTimeMillis();

        if (!plugin.marriedUsers.containsKey(uuid)) return;

        final PlayerMarry playerMarry = plugin.marriedUsers.get(plugin.marriedUsers.get(uuid).getMarryUUID());
        final Player partner = Bukkit.getPlayer(playerMarry.getUuid());

        if (!playerMarry.getMarryName().equals(playerName)) {
            playerMarry.setMarryName(playerName);
            Update update = new Update(plugin.getString(Strings.TABLE_MARRY));
            update.set.set("marry_name", playerName);
            update.where.set("uuid", playerMarry.getUuid());
            SQL.executeAsync(update);
        }
        if (!playerMarry.getMarryDisplay().equals(playerDisplay)) {
            playerMarry.setMarryDisplay(playerDisplay);
            Update update = new Update(plugin.getString(Strings.TABLE_MARRY));
            update.set.set("marry_display", playerDisplay);
            update.where.set("uuid", playerMarry.getUuid());
            SQL.executeAsync(update);
        }


        if (partner != null) {
            final int id = playerMarry.getMarryId();
            final MarryId marryId = plugin.marrieds.get(id);
            marryId.setMarryLast(time);
            
            if (plugin.marryOnline.getOrDefault(id, false).equals(Boolean.TRUE)) {
                updateTime(id);
            }

            Update update = new Update(plugin.getString(Strings.TABLE_BANK));
            update.set.set("last", time);
            update.where.set("marry_id", id);
            SQL.executeAsync(update);
            plugin.marrieds.put(id, marryId);
            plugin.marryOnline.put(id, true);
        }

        plugin.marriedUsers.put(playerMarry.getUuid(), playerMarry);
        plugin.userKiss.put(uuid, time);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        if (!plugin.marriedUsers.containsKey(uuid)) return;

        final PlayerMarry playerMarry = plugin.marriedUsers.get(uuid);
        final Player partner = Bukkit.getPlayer(playerMarry.getMarryUUID());

        if (partner != null) {
            final int id = playerMarry.getMarryId();
            updateTime(id);
            plugin.marryOnline.put(id, false);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        if (!(event.getRightClicked() instanceof Player)) return;
        final Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        final UUID uuid = player.getUniqueId();
        if (!plugin.userKiss.containsKey(uuid)) {
            plugin.userKiss.put(uuid, System.currentTimeMillis());
        }
        if (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - plugin.userKiss.get(uuid)) < 30) return;
        if (!plugin.marriedUsers.containsKey(uuid)) return;
        if (!manager.isReallyClose(player)) return;

        Location loc = player.getLocation();
        for (double angle = 0; angle < 2 * Math.PI; angle += 0.2) {
            final double x = 2 * Math.cos(angle);
            final double z = 2 * Math.sin(angle);
            loc.add(x, 1, z);
            loc.getWorld().spawnParticle(Particle.HEART, loc, 1);
            loc.subtract(x, 1, z);
        }
        plugin.userKiss.put(uuid, System.currentTimeMillis());

    }

    private void updateTime(int id) {
        final MarryId marryId = plugin.marrieds.get(id);
        int hours = marryId.getMarryHours() + (int) TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - marryId.getMarryLast());
        marryId.setMarryHours(hours);

        Update update = new Update(plugin.getString(Strings.TABLE_BANK));
        update.set.set("hours", hours);
        update.where.set("marry_id", id);
        SQL.executeAsync(update);
        plugin.marrieds.put(id, marryId);
    }

}
