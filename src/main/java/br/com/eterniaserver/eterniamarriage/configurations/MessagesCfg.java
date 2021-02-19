package br.com.eterniaserver.eterniamarriage.configurations;

import br.com.eterniaserver.eternialib.core.baseobjects.CustomizableMessage;
import br.com.eterniaserver.eternialib.core.enums.ConfigurationCategory;
import br.com.eterniaserver.eternialib.core.interfaces.ReloadableConfiguration;
import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.core.enums.Messages;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;


public class MessagesCfg implements ReloadableConfiguration {

    private final CustomizableMessage[] defaults = new CustomizableMessage[Messages.values().length];
    private final String[] messages;

    public MessagesCfg(final String[] messages) {
        this.messages = messages;
        this.addDefault(Messages.SERVER_LOADED, "$3{0} $7carregou $3{1} $7arquivos$8.", "0: modulo; 1: quantia");
        this.addDefault(Messages.SERVER_NO_NEGATIVE, "$7O valor precisa ser positivo$8.", null);
        this.addDefault(Messages.SERVER_NO_MONEY, "$7Você não possui todo esse dinheiro$8, $7você precisa de $3{0}$8.", "0: quantia");
        this.addDefault(Messages.SERVER_NOT_SAME, "$7Você não pode se casar uma pessoa com ela mesma$8.", null);
        this.addDefault(Messages.SERVER_TIMING, "$7Você será teleportado em $3{0} &7segundos$8.", "0: tempo em segundos");
        this.addDefault(Messages.SERVER_MOVE, "$7Você se moveu por isso o teleporte foi cancelado$8.", null);
        this.addDefault(Messages.SERVER_RELOAD, "$7Reiniciando o EterniaMarriage$8.", null);
        this.addDefault(Messages.MARRY_ALREADY_PROPOSAL, "$7Um dos jogadores já tem um pedido de casamento$8, $7rejeite-o antes de fazer um novo pedido$8.", null);
        this.addDefault(Messages.MARRY_ALREADY_SENT, "$7Esse jogador já tem um pedido de casamento em andamento$8.", null);
        this.addDefault(Messages.MARRY_SEND_PROPOSAL, "$7Você, $3{1}&8, $7aceita se casar com $3{3}$8?\n$8[$aE$9S$8] $7Use$8: $6/marry accept $7para aceitar ou $6/marry deny $7para negar$8.", "0: nome do jogador; 1: apelido do jogador; 2: nome do segundo jogador; 3: apelido do segundo jogador");
        this.addDefault(Messages.MARRY_ADVICE, "$3{1} $7e $3{3} $7estão se casando$8!", "0: nome do jogador; 1: apelido do jogador; 2: nome do segundo jogador; 3: apelido do terceiro jogador");
        this.addDefault(Messages.MARRY_ACCEPT, "$3%{1} $7aceitou$8!", "0: nome do jogador; 1: apelido do jogador");
        this.addDefault(Messages.MARRY_DENY, "$7Que triste$8... $3{1} $7negou o pedido$8.", "0: nome do jogador; 1: apelido do jogador");
        this.addDefault(Messages.MARRY_NO_PROPOSAL, "$7Nenhum pedido foi feito a você$8.", null);
        this.addDefault(Messages.MARRY_SUCESS, "$7Os dois aceitaram$8! $3{1} $7e $3{3}$7 acabaram de se casar$8!", "0: nome do segundo jogador; 1: apelido do segundo jogador; 2: nome do jogador; 3: apelido do jogador");
        this.addDefault(Messages.MARRY_NO_MARRY, "$7Esses jogadores(as) não são casados(as)$8.", null);
        this.addDefault(Messages.MARRY_ALREADY_MARRIED, "$7Um dos jogadores já é casado$8.", null);
        this.addDefault(Messages.MARRY_TIMEOUT, "$7A cerimonia durou muito$8, $7o casamento foi cancelado$8.", null);
        this.addDefault(Messages.COMMANDS_BALANCE, "$7Seu casal possui $3{0}$8.", "0: quantia no banco");
        this.addDefault(Messages.COMMANDS_NOT_MARRIED, "$7Você não é casado$8.", null);
        this.addDefault(Messages.COMMANDS_WITHDRAW, "$7Você retirou $3{0} $7do banco do casal$8.", "0: quantia de dinheiro");
        this.addDefault(Messages.COMMANDS_DEPOSIT, "$7Você depositou $3{0} $7no banco do casal$8.", "0: quantia no banco");
        this.addDefault(Messages.COMMANDS_RECEIVE, "$7Você sacou $3{0} $7do banco do casal$8.", "0: quantia de dinheiro");
        this.addDefault(Messages.COMMANDS_NOT_MONEY, "$7Seu casal não possui dinheiro suficiênte$8, $7você precisa de$3{0}$8.", "0: quantia");
        this.addDefault(Messages.COMMANDS_OFFLINE, "$7Seu parceiro está offline$8.", null);
        this.addDefault(Messages.COMMANDS_NO_HOME, "$7Vocês não possuem uma home ainda$8.", null);
        this.addDefault(Messages.COMMANDS_HOME_SAVE, "$7A nova casa de vocês foi definida e isso custou $3{0}$8.", "0: custo");
        this.addDefault(Messages.COMMANDS_HOME, "$7Lar doce lar$8!", null);
        this.addDefault(Messages.RELIGION_MAX_SIZE, "$7O {0} só pode possuir $3{1} $7caracteres$8.", "0: nome da configuração; 1: tamanho máximo");
        this.addDefault(Messages.RELIGION_UPDATED, "$7Você definiu o nome de sua religião como $3{0} $7e a prefix como $3{1}$8.", "0: nome da religião; 1: prefix da religião");
        this.addDefault(Messages.RELIGION_NOT, "$7Você ainda não possui religião ou convite$8.", null);
        this.addDefault(Messages.RELIGION_INVITED, "$7Você foi convidado para a religião$3{0}$7 aceite com $6/religion accept$7 ou negue com $6/religion deny$8.", "0: nome da religião");
        this.addDefault(Messages.RELIGION_INVITE_SENT, "$7Convite enviado com sucesso$8.", null);
        this.addDefault(Messages.RELIGION_ACCEPTED, "$3{1} $7entrou para a religião $3{2}$8.", "0: nome do jogador; 1: apelido do jogador; 2: nome da religião");
        this.addDefault(Messages.RELIGION_ALREADY, "$7Esse jogador já possui um convite de religião$8, $7aguarde$8.", null);
        this.addDefault(Messages.RELIGION_DENY, "$7Você negou o convite para a religião$8.", null);

    }

    private void addDefault(final Messages id, final String text, final String notes) {
        defaults[id.ordinal()] = new CustomizableMessage(text, notes);
    }

    @Override
    public ConfigurationCategory category() {
        return ConfigurationCategory.GENERIC;
    }

    @Override
    public void executeConfig() {

        final FileConfiguration config = YamlConfiguration.loadConfiguration(new File(Constants.MESSAGES_FILE_PATH));

        for (final Messages entry : Messages.values()) {
            CustomizableMessage messageData = defaults[entry.ordinal()];

            if (messageData == null) {
                messageData = new CustomizableMessage("Mensagem faltando para $3" + entry.name() + "$8.", null);
            }

            messages[entry.ordinal()] = config.getString(entry.name() + ".text", messageData.text);
            config.set(entry.name() + ".text", messages[entry.ordinal()]);

            messages[entry.ordinal()] = messages[entry.ordinal()].replace('$', (char) 0x00A7);

            if (messageData.getNotes() != null) {
                messageData.setNotes(config.getString(entry.name() + ".notes", messageData.getNotes()));
                config.set(entry.name() + ".notes", messageData.getNotes());
            }

        }

        try {
            config.save(Constants.MESSAGES_FILE_PATH);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void executeCritical() {

    }
}
