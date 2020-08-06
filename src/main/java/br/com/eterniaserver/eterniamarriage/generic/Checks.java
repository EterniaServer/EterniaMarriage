package br.com.eterniaserver.eterniamarriage.generic;

import br.com.eterniaserver.eternialib.EFiles;
import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.Strings;
import br.com.eterniaserver.paperlib.PaperLib;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Checks implements Runnable {

    private final EFiles messages;

    public Checks(EterniaMarriage plugin) {
        this.messages = plugin.getEFiles();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            final UUID uuid = UUIDFetcher.getUUIDOf(player.getName());
            if (APIMarry.isMarried(uuid)) {
                final Player partner = Bukkit.getPlayer(APIMarry.getPartnerUUID(uuid));
                if (partner != null && partner.isOnline()) {
                    getHealthRegen(player);
                }
                getPlayersInTp(player);
            }
        }
    }

    private void getHealthRegen(Player player) {
        if (APIMarry.isCloseToPartner(player)) {
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
                        messages.sendMessage(playerTeleport.getMessage(), player);
                        Vars.teleports.remove(player);
                    } else {
                        messages.sendMessage(Strings.M_SERVER_TIMING, Constants.COOLDOWN, playerTeleport.getCountdown(), player);
                        playerTeleport.decreaseCountdown();
                    }
                } else {
                    messages.sendMessage(Strings.M_SERVER_MOVE, player);
                    Vars.teleports.remove(player);
                }
            } else {
                PaperLib.teleportAsync(player, playerTeleport.getWantLocation());
                messages.sendMessage(playerTeleport.getMessage(), player);
                Vars.teleports.remove(player);
            }
        }
    }
}
