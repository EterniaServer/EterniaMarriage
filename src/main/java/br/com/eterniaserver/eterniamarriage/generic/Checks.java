package br.com.eterniaserver.eterniamarriage.generic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Checks implements Runnable {

    @Override
    public void run() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            final String partner = player.getName();
            if (APIMarry.isMarried(partner)) {

                final Player partnerPlayer = Bukkit.getPlayer(partner);
                final String marryName = APIMarry.getMarriedBankName(partner);
                if (partnerPlayer != null && partnerPlayer.isOnline()) {
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
                } else {
                    Vars.saveTime.remove(marryName);
                }

            }
        }

    }

}
