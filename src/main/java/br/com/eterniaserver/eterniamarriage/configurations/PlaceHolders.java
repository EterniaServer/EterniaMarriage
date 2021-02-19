package br.com.eterniaserver.eterniamarriage.configurations;

import br.com.eterniaserver.eternialib.UUIDFetcher;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.core.Manager;
import br.com.eterniaserver.eterniamarriage.core.enums.Strings;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nonnull;

import java.text.DecimalFormat;
import java.util.UUID;

public class PlaceHolders extends PlaceholderExpansion {

    private final EterniaMarriage plugin;
    private final String versionString;
    private final String ninguemStr;
    private final DecimalFormat df2;
    private final Manager manager;

    public PlaceHolders(final EterniaMarriage plugin, final Manager manager) {
        this.plugin = plugin;
        this.versionString = plugin.getClass().getPackage().getImplementationVersion();
        this.ninguemStr = "Ninguém";
        this.df2 = new DecimalFormat(".##");
        this.manager = manager;
    }

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
        return versionString;
    }

    @Override
    public String onRequest(OfflinePlayer p, @Nonnull String identifier) {
        return p != null ? getPlaceHolder(getIdentifier(identifier), UUIDFetcher.getUUIDOf(p.getName())) : "";
    }

    private byte getIdentifier(final String identifier) {
        switch(identifier.hashCode()) {
            case 2123281055:
                return 0;
            case 1276528444:
                return 1;
            case 975711363:
                return 2;
            case 975483731:
                return 3;
            case 839478272:
                return 4;
            case -693232709:
                return 5;
            case -792929080:
                return 6;
            case -883341420:
                return 7;
            case -906561999:
                return 8;
            case -979992193:
                return 9;
            case -1404969206:
                return 10;
            default:
                return 12;
        }
    }

    private String getPlaceHolder(final byte var4, final UUID uuid) {
        switch (var4) {
            // isclosetopartner
            case 0:
                return isCloseToPartner(uuid);
            // religionname
            case 1:
                return manager.getReligionName(uuid);
            // partneruuid
            case 2:
                return manager.isMarried(uuid) ? String.valueOf(plugin.marriedUsers.get(uuid).getMarryUUID()) : ninguemStr;
            // partnername
            case 3:
                return manager.isMarried(uuid) ? manager.getPartnerName(uuid) : ninguemStr;
            // marryid
            case 4:
                return manager.isMarried(uuid) ? String.valueOf(manager.getMarryId(uuid)) : "";
            // marrymoney
            case 5:
                return manager.isMarried(uuid) ? getMoney(uuid) : "";
            // partner
            case 6:
                return manager.isMarried(uuid) ? manager.getPartnerDisplay(uuid) : ninguemStr;
            // statusheart
            case 7:
                return getStatusHeart(uuid);
            // prieststatus
            case 8:
                return getPriestStatus(uuid);
            // priest
            case 9:
                return getPriest(uuid);
            // ismarried
            case 10:
                return isMarried(uuid);
            default:
                 return null;
        }
    }


    public String getPriestStatus(UUID uuid) {
        return Bukkit.getPlayer(uuid).hasPermission("eternia.priest") ? "&6" + manager.getReligionPrefix(uuid) : "&7" + plugin.getString(Strings.PLACEHOLDER_DEFAULT_RELIGION);
    }

    public String getPriest(UUID uuid) {
        return Bukkit.getPlayer(uuid).hasPermission("eternia.priest") ? "&6" + manager.getReligionPrefix(uuid) : "";
    }


    private String isCloseToPartner(final UUID uuid) {
        return isclose(uuid) ? plugin.getString(Strings.PLACEHOLDER_CLOSE_TO_PARTNER) : "";
    }

    private String isMarried(final UUID uuid) {
        return manager.isMarried(uuid) ? "&c" + plugin.getString(Strings.PLACEHOLDER_PARTNER) : "";
    }

    private String getStatusHeart(final UUID uuid) {
        return manager.isMarried(uuid) ? "&c" + plugin.getString(Strings.PLACEHOLDER_PARTNER) : "&7" + plugin.getString(Strings.PLACEHOLDER_PARTNER);
    }

    private String getMoney(final UUID uuid) {
        return df2.format(manager.getMarryMoney(manager.getMarryId(uuid)));
    }

    private boolean isclose(final UUID uuid) {
        return manager.isMarried(uuid) && manager.isCloseToPartner(Bukkit.getOfflinePlayer(uuid));
    }


}
