package br.com.eterniaserver.eterniamarriage;

public enum Constants {

    MODULE("%module%"),
    AMOUNT("%amount%"),
    PLAYER("%player_displayname%"),
    TARGET("%target_displayname%");

    private final String value;

    Constants(String value) {
        this.value = value;
    }

    public String get() {
        return value;
    }

}
