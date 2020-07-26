package br.com.eterniaserver.eterniamarriage.dependencies.eternialib;

import br.com.eterniaserver.eternialib.EQueries;
import br.com.eterniaserver.eternialib.EterniaLib;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;

public class Table {

    public Table() {

        EQueries.executeQuery("CREATE TABLE IF NOT EXISTS " + EterniaMarriage.serverConfig.getString("sql.table-marry")
                + " (player_name varchar(32), marry_name varchar(32));", false);
        if (EterniaLib.getMySQL()) {
            EQueries.executeQuery("CREATE TABLE IF NOT EXISTS " + EterniaMarriage.serverConfig.getString("sql.table-bank") +
                    " (marry_bank varchar(32), balance double(22,4), marry_time INTEGER, location varchar(128));", false);
        } else {
            EQueries.executeQuery("CREATE TABLE IF NOT EXISTS " + EterniaMarriage.serverConfig.getString("sql.table-bank") +
                    " (marry_bank varchar(32), balance double(22), marry_time INTEGER, location varchar(128));", false);
        }

    }

}
