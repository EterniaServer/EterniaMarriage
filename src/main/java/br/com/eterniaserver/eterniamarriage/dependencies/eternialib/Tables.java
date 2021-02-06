package br.com.eterniaserver.eterniamarriage.dependencies.eternialib;

import br.com.eterniaserver.eternialib.EterniaLib;
import br.com.eterniaserver.eternialib.SQL;
import br.com.eterniaserver.eternialib.sql.queries.CreateTable;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.enums.Strings;

public class Tables {

    public Tables() {
        
        final String marryId = "marry_id INT(8)";

        CreateTable createTable = new CreateTable(EterniaMarriage.getString(Strings.TABLE_RELIGION));
        createTable.columns.set("uuid VARCHAR(36)", "religion_name VARCHAR(36)", "religion_prefix VARCHAR(36)");
        SQL.execute(createTable);

        createTable = new CreateTable(EterniaMarriage.getString(Strings.TABLE_MARRY));
        createTable.columns.set("uuid VARCHAR(36)", "marry_uuid VARCHAR(36)", "marry_name VARCHAR(16)", "marry_display VARCHAR(16)", marryId);
        SQL.execute(createTable);

        if (EterniaLib.getMySQL()) {
            createTable = new CreateTable(EterniaMarriage.getString(Strings.TABLE_BANK));
            createTable.columns.set(marryId, "balance DOUBLE(22,4)", "hours INTEGER(4)", "location VARCHAR(64)", "time BIGINT(20)", "last BIGINT(20)");
            SQL.execute(createTable);
        } else {
            createTable = new CreateTable(EterniaMarriage.getString(Strings.TABLE_BANK));
            createTable.columns.set(marryId, "balance DOUBLE(22)", "hours INTEGER(4)", "location VARCHAR(64)", "time INTEGER", "last INTEGER");
            SQL.execute(createTable);
        }

    }

}
