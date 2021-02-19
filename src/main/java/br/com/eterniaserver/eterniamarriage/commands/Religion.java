package br.com.eterniaserver.eterniamarriage.commands;

import java.util.UUID;

import br.com.eterniaserver.eternialib.core.queries.Insert;
import br.com.eterniaserver.eternialib.core.queries.Update;

import org.bukkit.entity.Player;

import br.com.eterniaserver.acf.BaseCommand;
import br.com.eterniaserver.acf.annotation.CommandAlias;
import br.com.eterniaserver.acf.annotation.CommandCompletion;
import br.com.eterniaserver.acf.annotation.CommandPermission;
import br.com.eterniaserver.acf.annotation.Description;
import br.com.eterniaserver.acf.annotation.Subcommand;
import br.com.eterniaserver.acf.annotation.Syntax;
import br.com.eterniaserver.acf.bukkit.contexts.OnlinePlayer;
import br.com.eterniaserver.eternialib.SQL;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.core.enums.Doubles;
import br.com.eterniaserver.eterniamarriage.core.enums.Messages;
import br.com.eterniaserver.eterniamarriage.core.enums.Strings;
import br.com.eterniaserver.eterniamarriage.core.baseobjects.ReligionInvite;

@CommandAlias("religion")
public class Religion extends BaseCommand {

    private final EterniaMarriage plugin;

    public Religion(final EterniaMarriage plugin) {
        this.plugin = plugin;
    }

    @Subcommand("nome")
    @Syntax("<nome da religião> <prefix>")
    @CommandCompletion("Slimefunzeirismo sf")
    @CommandPermission("eternia.priest")
    @Description("Defina sua religião")
    public void setReligion(Player player, String nome, String prefix) {
        nome = nome.replaceAll("[^A-Za-z]", "");

        if (nome.length() > (int) plugin.getDouble(Doubles.RELIGION_MAX_NAME)) {
            plugin.sendMessage(player, Messages.RELIGION_MAX_SIZE, "nome", String.valueOf((int) plugin.getDouble(Doubles.RELIGION_MAX_NAME)));
            return;
        }

        if (prefix.length() > (int) plugin.getDouble(Doubles.RELIGION_MAX_PREFIX)) {
            plugin.sendMessage(player, Messages.RELIGION_MAX_SIZE, "prefix", String.valueOf((int) plugin.getDouble(Doubles.RELIGION_MAX_PREFIX)));
            return;
        }

        final UUID uuid = player.getUniqueId();
        br.com.eterniaserver.eterniamarriage.core.baseobjects.Religion religion = plugin.religions.get(uuid);


        plugin.sendMessage(player, Messages.RELIGION_UPDATED, nome, prefix);

        if (religion == null) {
            Insert insert = new Insert(plugin.getString(Strings.TABLE_RELIGION));
            insert.columns.set("uuid", "religion_name", "religion_prefix");
            insert.values.set(uuid, nome, prefix);
            SQL.executeAsync(insert);
            plugin.religions.put(uuid, new br.com.eterniaserver.eterniamarriage.core.baseobjects.Religion(nome, prefix));
            return;
        }

        religion.religionName = nome;
        religion.religionPrefix = prefix;
        Update update = new Update(plugin.getString(Strings.TABLE_RELIGION));
        update.set.set("religion_name", nome);
        update.where.set("uuid", uuid);
        SQL.executeAsync(update);
        update.set.set("religion_prefix", prefix);
        update.where.set("uuid", uuid);
        SQL.executeAsync(update);
    }

    @Subcommand("invite")
    @Syntax("<jogador>")
    @CommandCompletion("@players")
    @CommandPermission("eternia.priest")
    @Description("Convite alguém para sua religião")
    public void onInvite(Player player, OnlinePlayer convidado) {
        final UUID uuid = player.getUniqueId();
        final br.com.eterniaserver.eterniamarriage.core.baseobjects.Religion religion = plugin.religions.get(uuid);
        
        if (religion == null) {
            plugin.sendMessage(player, Messages.RELIGION_NOT);
            return;
        }

        final Player targetPlayer = convidado.getPlayer();
        final UUID targetUUID = targetPlayer.getUniqueId();

        if (plugin.invited.containsKey(targetUUID)) {
            plugin.sendMessage(player, Messages.RELIGION_ALREADY);
            return;
        }

        final ReligionInvite invite = new ReligionInvite();
        invite.religion = religion;
        invite.time = System.currentTimeMillis();
        plugin.invited.put(targetUUID, invite);

        plugin.sendMessage(targetPlayer, Messages.RELIGION_INVITED, religion.getReligionName());
        plugin.sendMessage(player, Messages.RELIGION_INVITE_SENT, religion.getReligionName());
    }

    @Subcommand("accept")
    @Description("Aceite o convite de uma religião")
    public void onAccept(Player player) {
        final UUID uuid = player.getUniqueId();

        if (!plugin.invited.containsKey(uuid)) {
            plugin.sendMessage(player, Messages.RELIGION_NOT);
            return;
        }

        ReligionInvite religionInvite = plugin.invited.get(uuid);
        plugin.getMessage(Messages.RELIGION_ACCEPTED, true, player.getName(), player.getDisplayName(), religionInvite.religion.getReligionName());
        plugin.religions.put(uuid, religionInvite.religion);
    }

    @Subcommand("deny")
    @Description("Negue o convite de uma religião")
    public void onDeny(Player player) {
        final UUID uuid = player.getUniqueId();

        if (!plugin.invited.containsKey(uuid)) {
            plugin.sendMessage(player, Messages.RELIGION_NOT);
            return;
        }

        plugin.invited.remove(uuid);
        plugin.sendMessage(player, Messages.RELIGION_DENY);
    }

}
