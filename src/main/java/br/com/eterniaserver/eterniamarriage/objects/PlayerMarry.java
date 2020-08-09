package br.com.eterniaserver.eterniamarriage.objects;

import java.util.UUID;

public class PlayerMarry {

    private UUID uuid;
    private UUID marryUUID;
    private String marryName;
    private String marryDisplay;
    private int marryId;

    public PlayerMarry(UUID uuid, UUID marryUUID, String marryName, String marryDisplay, int marryId) {
        this.uuid = uuid;
        this.marryUUID = marryUUID;
        this.marryName = marryName;
        this.marryDisplay = marryDisplay;
        this.marryId = marryId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getMarryUUID() {
        return marryUUID;
    }

    public void setMarryUUID(UUID marryUUID) {
        this.marryUUID = marryUUID;
    }

    public String getMarryName() {
        return marryName;
    }

    public void setMarryName(String marryName) {
        this.marryName = marryName;
    }

    public String getMarryDisplay() {
        return marryDisplay;
    }

    public void setMarryDisplay(String marryDisplay) {
        this.marryDisplay = marryDisplay;
    }

    public int getMarryId() {
        return marryId;
    }

    public void setMarryId(int marryId) {
        this.marryId = marryId;
    }
}
