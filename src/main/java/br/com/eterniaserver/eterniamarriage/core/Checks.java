package br.com.eterniaserver.eterniamarriage.core;

import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.enums.Messages;
import br.com.eterniaserver.eterniamarriage.objects.PlayerTeleport;
import br.com.eterniaserver.paperlib.PaperLib;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Checks extends BukkitRunnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            final UUID uuid = player.getUniqueId();
            if (APIMarry.isMarried(uuid)) {
                final Player partner = Bukkit.getPlayer(APIMarry.getPartnerUUID(uuid));
                if (partner != null) {
                    getHealthRegen(player);
                }
                getPlayersInTp(player);
            }
        }
        checkPlayers();
    }

    private void checkPlayers() {
        Vars.marryProposes.forEach((k, v) -> {
            if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - v.time) > 15) {
                Vars.marryProposes.remove(k);
                Bukkit.broadcastMessage(EterniaMarriage.getMessage(Messages.MARRY_TIMEOUT, true));
            }
        });
        Vars.invited.forEach((k, v) -> {
            if (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - v.time) > 30) {
                Vars.invited.remove(k);
            }
        });
    }

    private void getHealthRegen(Player player) {
        if (APIMarry.isCloseToPartner(player)) {
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
        if (Vars.teleports.containsKey(player)) {
            final PlayerTeleport playerTeleport = Vars.teleports.get(player);
            if (!player.hasPermission("eternia.timing.bypass")) {
                if (!playerTeleport.hasMoved()) {
                    if (playerTeleport.getCountdown() == 0) {
                        PaperLib.teleportAsync(player, playerTeleport.getWantLocation());
                        player.sendMessage(playerTeleport.getMessage());
                        Vars.teleports.remove(player);
                    } else {
                        EterniaMarriage.sendMessage(player, Messages.SERVER_TIMING, String.valueOf(playerTeleport.getCountdown()));
                        playerTeleport.decreaseCountdown();
                    }
                } else {
                    EterniaMarriage.sendMessage(player, Messages.SERVER_MOVE);
                    Vars.teleports.remove(player);
                }
            } else {
                PaperLib.teleportAsync(player, playerTeleport.getWantLocation());
                player.sendMessage(playerTeleport.getMessage());
                Vars.teleports.remove(player);
            }
        }
    }
}
