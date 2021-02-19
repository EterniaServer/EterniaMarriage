package br.com.eterniaserver.eterniamarriage.core.baseobjects;

import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.core.enums.Doubles;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerTeleport {

    private final Player player;
    private final Location firstLocation;
    private final Location wantLocation;
    private final String message;
    private int cooldown;

    public PlayerTeleport(final EterniaMarriage plugin, final Player player, final Location wantLocation, final String message) {
        this.player = player;
        this.firstLocation = player.getLocation();
        this.wantLocation = wantLocation;
        this.message = message;
        this.cooldown = (int) plugin.getDouble(Doubles.SERVER_COOLDOWN);
    }

    public boolean hasMoved() {
        if (firstLocation.getWorld() != player.getLocation().getWorld()) return true;
        return firstLocation.distanceSquared(player.getLocation()) != 0;
    }

    public int getCountdown() {
        return cooldown;
    }

    public void decreaseCountdown() {
        cooldown -= 1;
    }

    public Location getWantLocation() {
        return wantLocation;
    }

    public String getMessage() {
        return message;
    }

}
