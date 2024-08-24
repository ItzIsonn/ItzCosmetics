package me.itzisonn_.itzcosmetics.cosmetics;

import lombok.Getter;

@Getter
public class Category {
    private final String id;
    private final String name;

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }
}