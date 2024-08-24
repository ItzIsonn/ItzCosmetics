package me.itzisonn_.itzcosmetics.cosmetics;

import lombok.Getter;

@Getter
public class Rarity {
    private final String id;
    private final int cost;
    private final String name;

    public Rarity(String id, int cost, String name) {
        this.id = id;
        this.cost = cost;
        this.name = name;
    }
}