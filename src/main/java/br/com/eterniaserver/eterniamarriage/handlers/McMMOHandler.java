package br.com.eterniaserver.eterniamarriage.handlers;

import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.core.Manager;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class McMMOHandler implements Listener {

    private final EterniaMarriage plugin;
    private final Manager manager;

    public McMMOHandler(final EterniaMarriage plugin, final Manager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMcMMOPlayerXpGain(McMMOPlayerXpGainEvent event) {
        final Player player = event.getPlayer();

        if (!plugin.marriedUsers.containsKey(player.getUniqueId())) return;
        if (!manager.isCloseToPartner(player)) return;

        event.setRawXpGained((float) (event.getRawXpGained() * 1.25));
    }

}
