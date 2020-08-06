package br.com.eterniaserver.eterniamarriage.generic;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Vars {

    private Vars() {
        throw new IllegalStateException("Utility class");
    }

    protected static int marryIdList;

    protected static final Map<UUID, String> userCache = new HashMap<>();

    protected static final Map<UUID, Integer> marryId = new HashMap<>();
    protected static final Map<UUID, UUID> userMarry = new HashMap<>();
    protected static final Map<UUID, String> marryName = new HashMap<>();
    protected static final Map<UUID, String> marryDisplay = new HashMap<>();

    protected static final Map<Integer, Boolean> marryOnline = new HashMap<>();
    protected static final Map<Integer, Long> marryMade = new HashMap<>();
    protected static final Map<Integer, Long> marryLastSee = new HashMap<>();
    protected static final Map<Integer, Double> marryBankMoney = new HashMap<>();
    protected static final Map<Integer, Integer> marryHours = new HashMap<>();
    protected static final Map<Integer, Location> marryLocation = new HashMap<>();

    protected static final Map<Player, PlayerTeleport> teleports = new HashMap<>();

    protected static final Map<String, String> proMarry = new HashMap<>();
    protected static final Map<String, Boolean> resMarry = new HashMap<>();

}
