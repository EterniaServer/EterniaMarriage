package br.com.eterniaserver.eterniamarriage.generic;

import br.com.eterniaserver.eternialib.EFiles;
import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.Strings;
import br.com.eterniaserver.paperlib.PaperLib;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Checks implements Runnable {

    private final EFiles messages;

    public Checks(EterniaMarriage plugin) {
        this.messages = plugin.getEFiles();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            final String partner = player.getName();
            if (APIMarry.isMarried(partner)) {
                final Player partnerPlayer = Bukkit.getPlayer(partner);
                final String marryName = APIMarry.getMarriedBankName(partner);
                if (partnerPlayer != null && partnerPlayer.isOnline()) {
                    getLifePoint(player, marryName);
                } else {
                    Vars.saveTime.remove(marryName);
                }
            }
            getPlayersInTp(player);
        }
    }

    private void getLifePoint(Player player, final String marryName) {
        if (APIMarry.isCloseToPartner(player)) {
            double health = player.getHealth();
            if (health + 1 <= 20) {
                player.setHealth(health + 1);
            }
        }
        if (Vars.saveTime.containsKey(marryName)) {
            Vars.marryTime.put(marryName, System.currentTimeMillis() - Vars.saveTime.get(marryName));
        } else {
            Vars.saveTime.put(marryName, System.currentTimeMillis());
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
