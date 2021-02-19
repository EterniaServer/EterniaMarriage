package br.com.eterniaserver.eterniamarriage.core.baseobjects;

import org.bukkit.Location;

public class MarryId {

    private final int marryId;
    private double marryBalance;
    private int marryHours;
    private Location marryLocation;
    private long marryDone;
    private long marryLast;

    public MarryId(int marryId, double marryBalance, int marryHours, Location marryLocation, long marryDone, long marryLast) {
        this.marryId = marryId;
        this.marryBalance = marryBalance;
        this.marryHours = marryHours;
        this.marryLocation = marryLocation;
        this.marryDone = marryDone;
        this.marryLast = marryLast;
    }

    public int getMarryId() {
        return marryId;
    }

    public double getMarryBalance() {
        return marryBalance;
    }

    public void setMarryBalance(double marryBalance) {
        this.marryBalance = marryBalance;
    }

    public int getMarryHours() {
        return marryHours;
    }

    public void setMarryHours(int marryHours) {
        this.marryHours = marryHours;
    }

    public Location getMarryLocation() {
        return marryLocation;
    }

    public void setMarryLocation(Location marryLocation) {
        this.marryLocation = marryLocation;
    }

    public long getMarryDone() {
        return marryDone;
    }

    public void setMarryDone(long marryDone) {
        this.marryDone = marryDone;
    }

    public long getMarryLast() {
        return marryLast;
    }

    public void setMarryLast(long marryLast) {
        this.marryLast = marryLast;
    }

}
