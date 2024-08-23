package me.itzisonn_.itzcosmetics.menu.filter;

public enum ShowType {
    ALL,
    ONLY_BOUGHT,
    ONLY_NOT_BOUGHT,
    ONLY_EQUIPPED,
    ONLY_SALE;

    public String getConfigName() {
        return switch (this) {
            case ALL -> "all";
            case ONLY_BOUGHT -> "onlyBought";
            case ONLY_NOT_BOUGHT -> "onlyNotBought";
            case ONLY_EQUIPPED -> "onlyEquipped";
            case ONLY_SALE -> "onlySale";
        };
    }
}