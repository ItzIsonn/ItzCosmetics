package me.itzisonn_.itzcosmetics.menu;

public enum MenuType {
    COSMETICS,
    CONFIRM,
    FILTER;

    public String getConfigName() {
        return switch (this) {
            case COSMETICS -> "cosmeticsMenu";
            case CONFIRM -> "confirmMenu";
            case FILTER -> "filterMenu";
        };
    }
}