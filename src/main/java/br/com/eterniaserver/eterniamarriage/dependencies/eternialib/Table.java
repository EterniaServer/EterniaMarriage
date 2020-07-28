package br.com.eterniaserver.eterniamarriage.dependencies.eternialib;

import br.com.eterniaserver.eternialib.EQueries;
import br.com.eterniaserver.eternialib.EterniaLib;
import br.com.eterniaserver.eterniamarriage.Constants;

public class Table {

    public Table() {

        EQueries.executeQuery(Constants.getQueryCreateTable(Constants.TABLE_MARRY, "(player_name varchar(32), marry_name varchar(32))"), false);
        if (EterniaLib.getMySQL()) {
            EQueries.executeQuery(Constants.getQueryCreateTable(Constants.TABLE_BANK,
                    "(marry_bank varchar(32), " +
                            "balance double(22,4), " +
                            "marry_time INTEGER, " +
                            "location varchar(128))"), false);
        } else {
            EQueries.executeQuery(Constants.getQueryCreateTable(Constants.TABLE_BANK,
                    "(marry_bank varchar(32), " +
                            "balance double(22), " +
                            "marry_time INTEGER, " +
                            "location varchar(128))"), false);
        }

    }

}
