package br.com.eterniaserver.eterniamarriage.generic;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Vars {

    private Vars() {
        throw new IllegalStateException("Utility class");
    }

    protected static final Map<String, String> marry = new HashMap<>();
    protected static final Map<String, Double> marryBank = new HashMap<>();
    protected static final Map<String, Long> marryTime = new HashMap<>();
    protected static final Map<String, Location> marryLocation = new HashMap<>();

    protected static final Map<String, String> proMarry = new HashMap<>();
    protected static final Map<String, Boolean> resMarry = new HashMap<>();
    protected static final Map<String, Long> saveTime = new HashMap<>();

    protected static final Map<Player, PlayerTeleport> teleports = new HashMap<>();

}
