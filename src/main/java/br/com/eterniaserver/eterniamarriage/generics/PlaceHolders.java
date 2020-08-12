package br.com.eterniaserver.eterniamarriage.generics;

import br.com.eterniaserver.eternialib.UUIDFetcher;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nonnull;

import java.text.DecimalFormat;
import java.util.UUID;

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
    public String onRequest(OfflinePlayer p, @Nonnull String identifier) {
        return p != null ? getPlaceHolder(getIdentifier(identifier), UUIDFetcher.getUUIDOf(p.getName())) : "";
    }

    private byte getIdentifier(final String identifier) {
        switch(identifier.hashCode()) {
            case 2123281055:
                return 0;
            case 975711363:
                return 1;
            case 975483731:
                return 2;
            case 839478272:
                return 3;
            case -693232709:
                return 4;
            case -792929080:
                return 5;
            case -883341420:
                return 6;
            case -1404969206:
                return 7;
            default:
                return 12;
        }
    }

    private String getPlaceHolder(final byte var4, final UUID uuid) {
        switch (var4) {
            // isclosetopartner
            case 0:
                return isclose(uuid) ? EterniaMarriage.serverConfig.getString("placeholders.isclose") : "";
            // partneruuid
            case 1:
                return APIMarry.isMarried(uuid) ? String.valueOf(APIMarry.getPartnerUUID(uuid)) : "Ninguém";
            // partnername
            case 2:
                return APIMarry.isMarried(uuid) ? APIMarry.getPartnerName(uuid) : "Ninguém";
            // marryid
            case 3:
                return APIMarry.isMarried(uuid) ? String.valueOf(APIMarry.getMarryId(uuid)) : "";
            // marrymoney
            case 4:
                return APIMarry.isMarried(uuid) ? getMoney(uuid) : "";
            // partner
            case 5:
                return APIMarry.isMarried(uuid) ? APIMarry.getPartnerDisplay(uuid) : "Ninguém";
            // statusheart
            case 6:
                return APIMarry.isMarried(uuid) ? "&c❤" : "&7❤";
            // ismarried
            case 7:
                return APIMarry.isMarried(uuid) ? "&c❤" : "";
            default:
                 return null;
        }
    }

    private String getMoney(final UUID uuid) {
        return df2.format(APIMarry.getMarryMoney(APIMarry.getMarryId(uuid)));
    }

    private boolean isclose(final UUID uuid) {
        return APIMarry.isMarried(uuid) && APIMarry.isCloseToPartner(Bukkit.getOfflinePlayer(uuid));
    }

}
