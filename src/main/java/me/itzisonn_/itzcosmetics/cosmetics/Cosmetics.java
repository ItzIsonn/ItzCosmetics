package me.itzisonn_.itzcosmetics.cosmetics;

import lombok.Getter;
import me.itzisonn_.itzcosmetics.cosmetics.type.Type;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
public class Cosmetics {
    private final String id;
    private final String name;
    private final Material item;
    private final Type type;
    private final Rarity rarity;
    private final ArrayList<Category> categories;
    private final ArrayList<String> description;
    private final HashMap<String, Object> data;

    public Cosmetics(String id,
                     String name, Material item,
                     Type type, Rarity rarity, ArrayList<Category> categories, ArrayList<String> description,
                     HashMap<String, Object> data) {
        this.id = id;
        this.name = name;
        this.item = item;
        this.type = type;
        this.rarity = rarity;
        this.categories = categories;
        this.description = description;
        this.data = data;
    }

    @Override
    public String toString() {
        return type.getId() + ":" + id;
    }
}