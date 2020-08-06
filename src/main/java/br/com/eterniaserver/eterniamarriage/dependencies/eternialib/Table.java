package br.com.eterniaserver.eterniamarriage.dependencies.eternialib;

import br.com.eterniaserver.eternialib.EQueries;
import br.com.eterniaserver.eternialib.EterniaLib;
import br.com.eterniaserver.eterniamarriage.Constants;

public class Table {

    public Table() {

        EQueries.executeQuery(Constants.getQueryCreateTable(Constants.TABLE_CACHE, "(uuid varchar(36), " +
                "player_name varchar(16))"), false);
        EQueries.executeQuery(Constants.getQueryCreateTable(Constants.TABLE_MARRY, "(uuid varchar(36), " +
                "marry_uuid varchar(36), " +
                "marry_name varchar(16), " +
                "marry_display varchar(16), " +
                "marry_id int(8))"), false);
        if (EterniaLib.getMySQL()) {
            EQueries.executeQuery(Constants.getQueryCreateTable(Constants.TABLE_BANK, "(marry_id int(8), " +
                    "balance double(22,4), " +
                    "hours integer(4), " +
                    "location varchar(64), " +
                    "time bigint(20), " +
                    "last bigint(20))"), false);
        } else {
            EQueries.executeQuery(Constants.getQueryCreateTable(Constants.TABLE_BANK, "(marry_id int(8), " +
                    "balance double(22), " +
                    "hours integer(4), " +
                    "location varchar(64), " +
                    "time integer, " +
                    "last integer)"), false);
        }

    }

}
