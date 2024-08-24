package me.itzisonn_.itzcosmetics.cosmetics.type;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
public class Type {
    private final String id;
    private final String name;
    private final ActivatorType activatorType;
    private final String[] activatorArgs;
    private final HashMap<String, ArrayList<String>> actions;

    public Type(String id, String name,
                ActivatorType activatorType, String[] activatorArgs,
                HashMap<String, ArrayList<String>> actions) {
        this.id = id;
        this.name = name;
        this.activatorType = activatorType;
        this.activatorArgs = activatorArgs;
        this.actions = actions;
    }
}