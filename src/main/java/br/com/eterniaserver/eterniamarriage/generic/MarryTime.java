package br.com.eterniaserver.eterniamarriage.generic;

import br.com.eterniaserver.eternialib.EQueries;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;

public class MarryTime {

    public void saveTime() {
        Vars.marryTime.forEach((k, v) -> EQueries.executeQuery("UPDATE " + EterniaMarriage.serverConfig.getString("sql.table-bank") + " SET marry_time='" + v + "' WHERE marry_bank='" + k + "';"));
    }

}
