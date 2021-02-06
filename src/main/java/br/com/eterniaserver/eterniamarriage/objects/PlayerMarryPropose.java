package br.com.eterniaserver.eterniamarriage.objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerMarryPropose {

    public long time;

    private Boolean marryAccept;
    private final UUID wifeUUID;
    private final String wifeName;
    private final String wifeDisplayName;

    private final UUID husbandUUID;
    private final String husbandName;
    private final String husbandDisplayName;

    public PlayerMarryPropose(UUID wifeUUID, UUID husbandUUID) {
        final Player wife = Bukkit.getPlayer(wifeUUID);
        final Player husband = Bukkit.getPlayer(husbandUUID);

        this.wifeUUID = wifeUUID;
        this.wifeName = wife.getName();
        this.wifeDisplayName = wife.getDisplayName();

        this.husbandUUID = husbandUUID;
        this.husbandName = husband.getName();
        this.husbandDisplayName = husband.getDisplayName();
    }

    public boolean getMarryAccept() {
        return marryAccept;
    }

    public void setMarryAccept() {
        marryAccept = marryAccept != null;
    }

    public UUID getWifeUUID() {
        return wifeUUID;
    }

    public UUID getHusbandUUID() {
        return husbandUUID;
    }

    public String getWifeName() {
        return wifeName;
    }

    public String getHusbandName() {
        return husbandName;
    }

    public String getWifeDisplayName() {
        return wifeDisplayName;
    }

    public String getHusbandDisplayName() {
        return husbandDisplayName;
    }
}
