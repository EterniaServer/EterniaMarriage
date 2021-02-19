package br.com.eterniaserver.eterniamarriage.core;

import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.core.enums.Messages;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.PlayerTeleport;
import br.com.eterniaserver.paperlib.PaperLib;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Tick extends BukkitRunnable {

    private final EterniaMarriage plugin;
    private final Manager manager;

    public Tick(final EterniaMarriage plugin, final Manager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            final UUID uuid = player.getUniqueId();
            if (plugin.marriedUsers.containsKey(uuid)) {
                final Player partner = Bukkit.getPlayer(manager.getPartnerUUID(uuid));
                if (partner != null) {
                    getHealthRegen(player);
                }
                getPlayersInTp(player);
            }
        }
        checkPlayers();
    }

    private void checkPlayers() {
        plugin.marryProposes.forEach((k, v) -> {
            if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - v.time) > 15) {
                plugin.marryProposes.remove(k);
                Bukkit.broadcastMessage(plugin.getMessage(Messages.MARRY_TIMEOUT, true));
            }
        });
        plugin.invited.forEach((k, v) -> {
            if (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - v.time) > 30) {
                plugin.invited.remove(k);
            }
        });
    }

    private void getHealthRegen(Player player) {
        if (manager.isCloseToPartner(player)) {
            if (player.isDead()) return;
            final double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            final double health = player.getHealth();
            if (health < maxHealth) {
                double newHeart = health + 0.5;
                if (newHeart > 20.0) newHeart = 20.0;
                player.setHealth(newHeart);
            }
        }
    }


    private void getPlayersInTp(final Player player) {
        if (plugin.teleports.containsKey(player)) {
            final PlayerTeleport playerTeleport = plugin.teleports.get(player);
            if (!player.hasPermission("eternia.timing.bypass")) {
                if (!playerTeleport.hasMoved()) {
                    if (playerTeleport.getCountdown() == 0) {
                        PaperLib.teleportAsync(player, playerTeleport.getWantLocation());
                        player.sendMessage(playerTeleport.getMessage());
                        plugin.teleports.remove(player);
                    } else {
                        plugin.sendMessage(player, Messages.SERVER_TIMING, String.valueOf(playerTeleport.getCountdown()));
                        playerTeleport.decreaseCountdown();
                    }
                } else {
                    plugin.sendMessage(player, Messages.SERVER_MOVE);
                    plugin.teleports.remove(player);
                }
            } else {
                PaperLib.teleportAsync(player, playerTeleport.getWantLocation());
                player.sendMessage(playerTeleport.getMessage());
                plugin.teleports.remove(player);
            }
        }
    }
}
