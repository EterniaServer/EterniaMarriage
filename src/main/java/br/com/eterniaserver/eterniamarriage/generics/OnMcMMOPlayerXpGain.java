package br.com.eterniaserver.eterniamarriage.generics;

import br.com.eterniaserver.eternialib.UUIDFetcher;

import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class OnMcMMOPlayerXpGain implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onGainXp(McMMOPlayerXpGainEvent event) {
        final Player player = event.getPlayer();
        final String playerName = player.getName();
        if (APIMarry.isMarried(UUIDFetcher.getUUIDOf(playerName))) {
            if (APIMarry.isCloseToPartner(player)) {
                event.setRawXpGained((float) (event.getRawXpGained() * 1.25));
            }
        }
    }

}
