package br.com.eterniaserver.eterniamarriage.core;

import br.com.eterniaserver.eternialib.CommandManager;
import br.com.eterniaserver.eternialib.SQL;
import br.com.eterniaserver.eternialib.core.queries.Update;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;

import br.com.eterniaserver.eterniamarriage.commands.Marry;
import br.com.eterniaserver.eterniamarriage.commands.Religion;
import br.com.eterniaserver.eterniamarriage.configurations.PlaceHolders;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.MarryId;
import br.com.eterniaserver.eterniamarriage.core.enums.Strings;
import br.com.eterniaserver.eterniamarriage.handlers.McMMOHandler;
import br.com.eterniaserver.eterniamarriage.handlers.PlayerHandler;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class Manager {

    private final EterniaMarriage plugin;

    public Manager(final EterniaMarriage plugin) {
        this.plugin = plugin;

        loadVault();
        registerCommands();

        plugin.getServer().getPluginManager().registerEvents(new PlayerHandler(plugin, this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new McMMOHandler(plugin, this), plugin);

        new PlaceHolders(plugin, this).register();
        new Tick(plugin, this).runTaskTimer(plugin, 0L, 20L);
    }

    private void loadVault() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }

        final RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return;
        }

        plugin.economy = rsp.getProvider();
    }

    private void registerCommands() {
        CommandManager.registerCommand(new Marry(plugin, this));
        CommandManager.registerCommand(new Religion(plugin));
    }

    public boolean isMarried(UUID uuid) {
        return plugin.marriedUsers.containsKey(uuid);
    }

    public UUID getPartnerUUID(UUID uuid) {
        return plugin.marriedUsers.get(uuid).getMarryUUID();
    }

    public String getPartnerName(UUID uuid) {
        return plugin.marriedUsers.get(uuid).getMarryName();
    }

    public String getReligionName(UUID uuid) {
        br.com.eterniaserver.eterniamarriage.core.baseobjects.Religion religion = plugin.religions.get(uuid);
        return religion == null ? "Nenhuma" : religion.religionName;
    }

    public String getReligionPrefix(UUID uuid) {
        br.com.eterniaserver.eterniamarriage.core.baseobjects.Religion religion = plugin.religions.get(uuid);
        return religion == null ? plugin.getString(Strings.PLACEHOLDER_DEFAULT_RELIGION) : religion.religionPrefix;
    }

    public String getPartnerDisplay(UUID uuid) {
        return plugin.marriedUsers.get(uuid).getMarryDisplay();
    }

    public boolean isCloseToPartner(OfflinePlayer player) {
        final Location location = player.getPlayer().getLocation();
        final OfflinePlayer partner = Bukkit.getOfflinePlayer(getPartnerUUID(player.getUniqueId()));
        if (partner.isOnline()) {
            final Location partnerLocation = partner.getPlayer().getLocation();
            return partnerLocation.getWorld() == location.getWorld() && partnerLocation.distanceSquared(location) <= 100;
        }
        return false;
    }

    public boolean isReallyClose(OfflinePlayer player) {
        final OfflinePlayer partner = Bukkit.getOfflinePlayer(getPartnerUUID(player.getUniqueId()));
        if (partner.isOnline()) {
            final Location location = player.getPlayer().getLocation();
            final Location partnerLocation = partner.getPlayer().getLocation();
            return partnerLocation.getWorld() == location.getWorld() && partnerLocation.distanceSquared(location) <= 2;
        }
        return false;
    }

    public int getMarryId(UUID uuid) {
        return plugin.marriedUsers.get(uuid).getMarryId();
    }

    public double getMarryMoney(int id) {
        return plugin.marrieds.get(id).getMarryBalance();
    }

    public void setMarryMoney(int id, double amount) {
        final MarryId marryId = plugin.marrieds.get(id);
        marryId.setMarryBalance(amount);

        Update update = new Update(plugin.getString(Strings.TABLE_BANK));
        update.set.set("balance", amount);
        update.where.set("marry_id", id);
        SQL.executeAsync(update);

        plugin.marrieds.put(id, marryId);
    }

    public void giveMarryBankMoney(int id, double amount) {
        setMarryMoney(id, getMarryMoney(id) + amount);
    }

    public void removeMarryBankMoney(int id, double amount) {
        setMarryMoney(id, getMarryMoney(id) - amount);
    }

}
