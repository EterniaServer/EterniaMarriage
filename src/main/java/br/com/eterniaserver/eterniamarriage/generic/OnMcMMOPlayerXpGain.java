package br.com.eterniaserver.eterniamarriage.generic;

import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class OnMcMMOPlayerXpGain implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onGainXp(McMMOPlayerXpGainEvent event) {
        if (APIMarry.isCloseToPartner(event.getPlayer())) {
            event.setRawXpGained((float) (event.getRawXpGained() * 1.25));
        }
    }

}
