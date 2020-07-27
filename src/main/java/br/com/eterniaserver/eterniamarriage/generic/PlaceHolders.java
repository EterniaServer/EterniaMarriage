package br.com.eterniaserver.eterniamarriage.generic;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class PlaceHolders extends PlaceholderExpansion {

    private final DecimalFormat df2 = new DecimalFormat(".##");
    private final String version = this.getClass().getPackage().getImplementationVersion();

    public String getAuthor() {
        return "yurinogueira";
    }

    public String getIdentifier() {
        return "eterniamarriage";
    }

    public String getVersion() {
        return this.version;
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        if (p == null) {
            return "";
        } else {
            return getPlaceHolder(getIdentifier(identifier), p.getName());
        }
    }

    private byte getIdentifier(final String identifier) {
        switch(identifier.hashCode()) {
            case -715082704:
                return 0;
            case -792929080:
                return 1;
            case -883341420:
                return 2;
            case -1404969206:
                return 3;
            case -1774411772:
                return 4;
            default:
                return 12;
        }
    }

    private String getPlaceHolder(final byte var4, final String playerName) {
        switch (var4) {
            case 0:
                if (APIMarry.isMarried(playerName)) return APIMarry.getMarriedBankName(playerName);
                else return "";
            case 1:
                if (APIMarry.isMarried(playerName)) return APIMarry.getPartner(playerName);
                else return "Ninguém";
            case 2:
                if (APIMarry.isMarried(playerName)) return "&c❤";
                else return "&7❤";
            case 3:
                if (APIMarry.isMarried(playerName)) return "&c❤";
                else return "";
            case 4:
                if (APIMarry.isMarried(playerName)) return df2.format(APIMarry.getMarryBankMoney(APIMarry.getMarriedBankName(playerName)));
                else return "";
             default:
                 return null;
        }
    }

}
