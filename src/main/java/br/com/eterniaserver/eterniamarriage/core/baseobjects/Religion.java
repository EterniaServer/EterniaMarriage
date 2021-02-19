package br.com.eterniaserver.eterniamarriage.core.baseobjects;

public class Religion {
    
    public String religionName;
    public String religionPrefix;

    public Religion(String religionName, String religionPrefix) {
        this.religionName = religionName;
        this.religionPrefix = religionPrefix;
    }

    public String getReligionName() {
        return religionName;
    }

    public String getReligionPrefix() {
        return religionPrefix;
    }

}
