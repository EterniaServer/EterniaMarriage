package br.com.eterniaserver.eterniamarriage.generic;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.OfflinePlayer;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;

public class PlaceHolders extends PlaceholderExpansion {

    private final DecimalFormat df2 = new DecimalFormat(".##");
    private final String version = this.getClass().getPackage().getImplementationVersion();

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    @Nonnull
    public String getAuthor() {
        return "yurinogueira";
    }

    @Override
    @Nonnull
    public String getIdentifier() {
        return "eterniamarriage";
    }

    @Override
    @Nonnull
    public String getVersion() {
        return this.version;
    }

    @Override
    @SuppressWarnings("deprecation")
    public String onRequest(OfflinePlayer p, @Nonnull String identifier) {
        return p != null ? getPlaceHolder(getIdentifier(identifier), p.getName()) : "";
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
                return APIMarry.isMarried(playerName) ? APIMarry.getMarriedBankName(playerName) : "";
            case 1:
                return APIMarry.isMarried(playerName) ? APIMarry.getPartner(playerName) : "Ninguém";
            case 2:
                return APIMarry.isMarried(playerName) ? "&c❤" : "&7❤";
            case 3:
                return APIMarry.isMarried(playerName) ? "&c❤" : "";
            case 4:
                return APIMarry.isMarried(playerName) ? getMoney(playerName) : "";
            default:
                 return null;
        }
    }

    private String getMoney(final String playerName) {
        return df2.format(APIMarry.getMarryBankMoney(APIMarry.getMarriedBankName(playerName)));
    }

}
