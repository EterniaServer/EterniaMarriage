package br.com.eterniaserver.eterniamarriage.dependencies;

import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.enums.Doubles;
import br.com.eterniaserver.eterniamarriage.enums.Messages;
import br.com.eterniaserver.eterniamarriage.enums.Strings;
import br.com.eterniaserver.eterniamarriage.objects.CustomizableMessage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigsCfg {

    public ConfigsCfg(String[] strings, Double[] doubles, String[] messages) {

        Map<String, CustomizableMessage> defaults = new HashMap<>();

        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(Constants.CONFIG_FILE_PATH));
        FileConfiguration messagesConfig = YamlConfiguration.loadConfiguration(new File(Constants.MESSAGES_FILE_PATH));
        FileConfiguration outConfig = new YamlConfiguration();

        strings[Strings.TABLE_MARRY.ordinal()] = config.getString("sql.table-marry", "em_players");
        strings[Strings.TABLE_BANK.ordinal()] = config.getString("sql.table-bank", "em_ids");
        strings[Strings.TABLE_RELIGION.ordinal()] = config.getString("sql.table-religion", "em_religion");
        strings[Strings.SERVER_PREFIX.ordinal()] = config.getString("server.prefix", "$8[$aE$9M$8]$7 ").replace('$', (char) 0x00A7);
        strings[Strings.PLACEHOLDER_CLOSE_TO_PARTNER.ordinal()] = config.getString("placeholders.isclose", "$3Perto").replace('$', (char) 0x00A7);
        strings[Strings.PLACEHOLDER_DEFAULT_RELIGION.ordinal()] = config.getString("placeholders.default-religion", "♰").replace('$', (char) 0x00A7);
        strings[Strings.PLACEHOLDER_PARTNER.ordinal()] = config.getString("placeholders.partner", "❤").replace('$', (char) 0x00A7);

        doubles[Doubles.MARRY_COST.ordinal()] = config.getDouble("money.marry", 200000.0);
        doubles[Doubles.SETHOME_COST.ordinal()] = config.getDouble("money.sethome", 2000.0);
        doubles[Doubles.SERVER_COOLDOWN.ordinal()] = config.getDouble("server.cooldown", 4.0);
        doubles[Doubles.RELIGION_MAX_NAME.ordinal()] = config.getDouble("religion.max-name", 12.0);
        doubles[Doubles.RELIGION_MAX_PREFIX.ordinal()] = config.getDouble("religion.max-prefix", 1.0);


        outConfig.set("sql.table-marry", strings[Strings.TABLE_MARRY.ordinal()]);
        outConfig.set("sql.table-bank", strings[Strings.TABLE_BANK.ordinal()]);
        outConfig.set("sql.table-religion", strings[Strings.TABLE_RELIGION.ordinal()]);
        outConfig.set("server.prefix", strings[Strings.SERVER_PREFIX.ordinal()]);
        outConfig.set("placeholders.isclose", strings[Strings.PLACEHOLDER_CLOSE_TO_PARTNER.ordinal()]);
        outConfig.set("placeholders.default-religion", strings[Strings.PLACEHOLDER_DEFAULT_RELIGION.ordinal()]);
        outConfig.set("placeholders.partner", strings[Strings.PLACEHOLDER_PARTNER.ordinal()]);

        outConfig.set("money.marry", doubles[Doubles.MARRY_COST.ordinal()]);
        outConfig.set("money.sethome", doubles[Doubles.SETHOME_COST.ordinal()]);
        outConfig.set("server.cooldown", doubles[Doubles.SERVER_COOLDOWN.ordinal()]);
        outConfig.set("religion.max-name", doubles[Doubles.RELIGION_MAX_NAME.ordinal()]);
        outConfig.set("religion.max-prefix", doubles[Doubles.RELIGION_MAX_PREFIX.ordinal()]);

        outConfig.options().header("Caso precise de ajuda acesse https://github.com/EterniaServer/EterniaMarriage/wiki");

        this.addDefault(defaults, Messages.SERVER_LOADED, "$3{0} $7carregou $3{1} $7arquivos$8.", "0: modulo; 1: quantia");
        this.addDefault(defaults, Messages.SERVER_NO_NEGATIVE, "$7O valor precisa ser positivo$8.", null);
        this.addDefault(defaults, Messages.SERVER_NO_MONEY, "$7Você não possui todo esse dinheiro$8, $7você precisa de $3{0}$8.", "0: quantia");
        this.addDefault(defaults, Messages.SERVER_NOT_SAME, "$7Você não pode se casar uma pessoa com ela mesma$8.", null);
        this.addDefault(defaults, Messages.SERVER_TIMING, "$7Você será teleportado em $3{0} &7segundos$8.", "0: tempo em segundos");
        this.addDefault(defaults, Messages.SERVER_MOVE, "$7Você se moveu por isso o teleporte foi cancelado$8.", null);
        this.addDefault(defaults, Messages.SERVER_RELOAD, "$7Reiniciando o EterniaMarriage$8.", null);
        this.addDefault(defaults, Messages.MARRY_ALREADY_PROPOSAL, "$7Um dos jogadores já tem um pedido de casamento$8, $7rejeite-o antes de fazer um novo pedido$8.", null);
        this.addDefault(defaults, Messages.MARRY_ALREADY_SENT, "$7Esse jogador já tem um pedido de casamento em andamento$8.", null);
        this.addDefault(defaults, Messages.MARRY_SEND_PROPOSAL, "$7Você, $3{1}&8, $7aceita se casar com $3{3}$8?\n$8[$aE$9S$8] $7Use$8: $6/marry accept $7para aceitar ou $6/marry deny $7para negar$8.", "0: nome do jogador; 1: apelido do jogador; 2: nome do segundo jogador; 3: apelido do segundo jogador");
        this.addDefault(defaults, Messages.MARRY_ADVICE, "$3{1} $7e $3{3} $7estão se casando$8!",  "0: nome do jogador; 1: apelido do jogador; 2: nome do segundo jogador; 3: apelido do terceiro jogador");
        this.addDefault(defaults, Messages.MARRY_ACCEPT, "$3%{1} $7aceitou$8!", "0: nome do jogador; 1: apelido do jogador");
        this.addDefault(defaults, Messages.MARRY_DENY, "$7Que triste$8... $3{1} $7negou o pedido$8.", "0: nome do jogador; 1: apelido do jogador");
        this.addDefault(defaults, Messages.MARRY_NO_PROPOSAL, "$7Nenhum pedido foi feito a você$8.", null);
        this.addDefault(defaults, Messages.MARRY_SUCESS, "$7Os dois aceitaram$8! $3{1} $7e $3{3}$7 acabaram de se casar$8!", "0: nome do segundo jogador; 1: apelido do segundo jogador; 2: nome do jogador; 3: apelido do jogador");
        this.addDefault(defaults, Messages.MARRY_NO_MARRY, "$7Esses jogadores(as) não são casados(as)$8.", null);
        this.addDefault(defaults, Messages.MARRY_ALREADY_MARRIED, "$7Um dos jogadores já é casado$8.", null);
        this.addDefault(defaults, Messages.MARRY_TIMEOUT, "$7A cerimonia durou muito$8, $7o casamento foi cancelado$8.", null);
        this.addDefault(defaults, Messages.COMMANDS_BALANCE, "$7Seu casal possui $3{0}$8.", "0: quantia no banco");
        this.addDefault(defaults, Messages.COMMANDS_NOT_MARRIED, "$7Você não é casado$8.", null);
        this.addDefault(defaults, Messages.COMMANDS_WITHDRAW, "$7Você retirou $3{0} $7do banco do casal$8.", "0: quantia de dinheiro");
        this.addDefault(defaults, Messages.COMMANDS_DEPOSIT, "$7Você depositou $3{0} $7no banco do casal$8.", "0: quantia no banco");
        this.addDefault(defaults, Messages.COMMANDS_RECEIVE, "$7Você sacou $3{0} $7do banco do casal$8.", "0: quantia de dinheiro");
        this.addDefault(defaults, Messages.COMMANDS_NOT_MONEY, "$7Seu casal não possui dinheiro suficiênte$8, $7você precisa de$3{0}$8.", "0: quantia");
        this.addDefault(defaults, Messages.COMMANDS_OFFLINE, "$7Seu parceiro está offline$8.", null);
        this.addDefault(defaults, Messages.COMMANDS_NO_HOME, "$7Vocês não possuem uma home ainda$8.", null);
        this.addDefault(defaults, Messages.COMMANDS_HOME_SAVE, "$7A nova casa de vocês foi definida e isso custou $3{0}$8.", "0: custo");
        this.addDefault(defaults, Messages.COMMANDS_HOME, "$7Lar doce lar$8!", null);
        this.addDefault(defaults, Messages.RELIGION_MAX_SIZE, "$7O {0} só pode possuir $3{1} $7caracteres$8.", "0: nome da configuração; 1: tamanho máximo");
        this.addDefault(defaults, Messages.RELIGION_UPDATED, "$7Você definiu o nome de sua religião como $3{0} $7e a prefix como $3{1}$8.", "0: nome da religião; 1: prefix da religião");
        this.addDefault(defaults, Messages.RELIGION_NOT, "$7Você ainda não possui religião ou convite$8.", null);
        this.addDefault(defaults, Messages.RELIGION_INVITED, "$7Você foi convidado para a religião$3{0}$7 aceite com $6/religion accept$7 ou negue com $6/religion deny$8.", "0: nome da religião");
        this.addDefault(defaults, Messages.RELIGION_INVITE_SENT, "$7Convite enviado com sucesso$8.", null);
        this.addDefault(defaults, Messages.RELIGION_ACCEPTED, "$3{1} $7entrou para a religião $3{2}$8.", "0: nome do jogador; 1: apelido do jogador; 2: nome da religião");
        this.addDefault(defaults, Messages.RELIGION_ALREADY, "$7Esse jogador já possui um convite de religião$8, $7aguarde$8.", null);
        this.addDefault(defaults, Messages.RELIGION_DENY, "$7Você negou o convite para a religião$8.", null);

        for (Messages messagesEnum : Messages.values()) {
            CustomizableMessage messageData = defaults.get(messagesEnum.name());

            if (messageData == null) {
                messageData = new CustomizableMessage(messagesEnum, EterniaMarriage.getString(Strings.SERVER_PREFIX) + "Mensagem faltando para $3" + messagesEnum.name() + "$8.", null);
            }

            messages[messagesEnum.ordinal()] = messagesConfig.getString(messagesEnum.name() + ".text", messageData.text);
            messagesConfig.set(messagesEnum.name() + ".text", messages[messagesEnum.ordinal()]);

            messages[messagesEnum.ordinal()] = messages[messagesEnum.ordinal()].replace('$', (char) 0x00A7);

            if (messageData.getNotes() != null) {
                messageData.setNotes(messagesConfig.getString(messagesEnum.name() + ".notes", messageData.getNotes()));
                messagesConfig.set(messagesEnum.name() + ".notes", messageData.getNotes());
            }

        }

        try {
            outConfig.save(Constants.CONFIG_FILE_PATH);
            messagesConfig.save(Constants.MESSAGES_FILE_PATH);
        } catch (IOException exception) {
            // todo
        }

    }

    private void addDefault(Map<String, CustomizableMessage> defaults, Messages id, String text, String notes) {
        CustomizableMessage message = new CustomizableMessage(id, text, notes);
        defaults.put(id.name(), message);
    }

}
