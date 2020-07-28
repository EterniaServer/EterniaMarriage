package br.com.eterniaserver.eterniamarriage.generic;

import br.com.eterniaserver.eternialib.EQueries;
import br.com.eterniaserver.eterniamarriage.Constants;
import br.com.eterniaserver.eterniamarriage.EterniaMarriage;
import br.com.eterniaserver.eterniamarriage.Strings;

public class MarryTime {

    public void saveTime() {
        Vars.marryTime.forEach((k, v) -> EQueries.executeQuery(Constants.getQueryUpdate(Constants.TABLE_BANK, Strings.MARRY_TIME, v, Strings.MARRY_BANK, k)));
    }

}
