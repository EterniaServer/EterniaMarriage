package br.com.eterniaserver.eterniamarriage.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
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
import br.com.eterniaserver.eternialib.UUIDFetcher;
import br.com.eterniaserver.eternialib.sql.queries.Insert;
import br.com.eterniaserver.eternialib.sql.queries.Select;
import br.com.eterniaserver.eternialib.sql.queries.Update;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.enums.Doubles;
import br.com.eterniaserver.eterniamarriage.enums.Messages;
import br.com.eterniaserver.eterniamarriage.enums.Strings;
import br.com.eterniaserver.eterniamarriage.objects.Religion;
import br.com.eterniaserver.eterniamarriage.objects.ReligionInvite;

@CommandAlias("religion")
public class ReligionCommand extends BaseCommand {
    
    public ReligionCommand() {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(new Select(EterniaMarriage.getString(Strings.TABLE_RELIGION)).queryString()); ResultSet resultSet = preparedStatement.executeQuery()) {
            Map<String, Religion> religionCache = new HashMap<>();
            while (resultSet.next()) {
                final String religionName = resultSet.getString("religion_name");
                if (religionCache.containsKey(religionName)) {
                    Vars.religions.put(UUID.fromString(resultSet.getString("uuid")), religionCache.get(religionName));
                } else {
                    Religion religion = new Religion(religionName, resultSet.getString("religion_prefix"));
                    religionCache.put(religionName, religion);
                    Vars.religions.put(UUID.fromString(resultSet.getString("uuid")), religion);
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        Bukkit.getConsoleSender().sendMessage(EterniaMarriage.getMessage(Messages.SERVER_LOADED, true, "Religions", String.valueOf(Vars.religions.size())));
    }

    @Subcommand("nome")
    @Syntax("<nome da religião> <prefix>")
    @CommandCompletion("Slimefunzeirismo sf")
    @CommandPermission("eternia.priest")
    @Description("Defina sua religião")
    public void setReligion(Player player, String nome, String prefix) {
        nome = nome.replaceAll("[^A-Za-z]", "");

        if (nome.length() > (int) EterniaMarriage.getDouble(Doubles.RELIGION_MAX_NAME)) {
            EterniaMarriage.sendMessage(player, Messages.RELIGION_MAX_SIZE, "nome", String.valueOf((int) EterniaMarriage.getDouble(Doubles.RELIGION_MAX_NAME)));
            return;
        }

        if (prefix.length() > (int) EterniaMarriage.getDouble(Doubles.RELIGION_MAX_PREFIX)) {
            EterniaMarriage.sendMessage(player, Messages.RELIGION_MAX_SIZE, "prefix", String.valueOf((int) EterniaMarriage.getDouble(Doubles.RELIGION_MAX_PREFIX)));
            return;
        }

        final UUID uuid = player.getUniqueId();
        Religion religion = Vars.religions.get(uuid);
        
        
        EterniaMarriage.sendMessage(player, Messages.RELIGION_UPDATED, nome, prefix);

        if (religion == null) {
            Insert insert = new Insert(EterniaMarriage.getString(Strings.TABLE_RELIGION));
            insert.columns.set("uuid", "religion_name", "religion_prefix");
            insert.values.set(uuid, nome, prefix);
            SQL.executeAsync(insert);
            Vars.religions.put(uuid, new Religion(nome, prefix));
            return;
        }

        religion.religionName = nome;
        religion.religionPrefix = prefix;
        Update update = new Update(EterniaMarriage.getString(Strings.TABLE_RELIGION));
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
        final Religion religion = Vars.religions.get(uuid);
        
        if (religion == null) {
            EterniaMarriage.sendMessage(player, Messages.RELIGION_NOT);
            return;
        }

        final Player targetPlayer = convidado.getPlayer();
        final UUID targetUUID = targetPlayer.getUniqueId();

        if (Vars.invited.containsKey(targetUUID)) {
            EterniaMarriage.sendMessage(player, Messages.RELIGION_ALREADY);
            return;
        }

        final ReligionInvite invite = new ReligionInvite();
        invite.religion = religion;
        invite.time = System.currentTimeMillis();
        Vars.invited.put(targetUUID, invite);
        
        EterniaMarriage.sendMessage(targetPlayer, Messages.RELIGION_INVITED, religion.getReligionName());
        EterniaMarriage.sendMessage(player, Messages.RELIGION_INVITE_SENT, religion.getReligionName());
    }

    @Subcommand("accept")
    @Description("Aceite o convite de uma religião")
    public void onAccept(Player player) {
        final UUID uuid = player.getUniqueId();

        if (!Vars.invited.containsKey(uuid)) {
            EterniaMarriage.sendMessage(player, Messages.RELIGION_NOT);
            return;
        }

        ReligionInvite religionInvite = Vars.invited.get(uuid);
        EterniaMarriage.getMessage(Messages.RELIGION_ACCEPTED, true, player.getName(), player.getDisplayName(), religionInvite.religion.getReligionName());
        Vars.religions.put(uuid, religionInvite.religion);
    }

    @Subcommand("deny")
    @Description("Negue o convite de uma religião")
    public void onDeny(Player player) {
        final UUID uuid = player.getUniqueId();

        if (!Vars.invited.containsKey(uuid)) {
            EterniaMarriage.sendMessage(player, Messages.RELIGION_NOT);
            return;
        }

        Vars.invited.remove(uuid);
        EterniaMarriage.sendMessage(player, Messages.RELIGION_DENY);
    }

}
