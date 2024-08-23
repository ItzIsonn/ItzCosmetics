package me.itzisonn_.itzcosmetics.menu.filter;

public enum SortType {
    TYPE,
    COST_INTURN,
    COST_INREVERSE;

    public String getConfigName() {
        return switch (this) {
            case TYPE -> "type";
            case COST_INTURN -> "costInturn";
            case COST_INREVERSE -> "costInreverse";
        };
    }
}