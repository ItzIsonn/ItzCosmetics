package me.itzisonn_.itzcosmetics.command;

import com.google.common.collect.Lists;
import me.itzisonn_.itzcosmetics.ItzCosmetics;
import me.itzisonn_.itzcosmetics.cosmetics.Category;
import me.itzisonn_.itzcosmetics.cosmetics.Cosmetics;
import me.itzisonn_.itzcosmetics.cosmetics.Rarity;
import me.itzisonn_.itzcosmetics.cosmetics.type.Type;
import me.itzisonn_.itzcosmetics.shop.Sale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TabCompleter extends AbstractTab {
    private final ItzCosmetics plugin;

    public TabCompleter(ItzCosmetics plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public ArrayList<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            ArrayList<String> list = new ArrayList<>();
            if (sender.hasPermission("itzcosmetics.reload")) list.add("reload");
            if (sender.hasPermission("itzcosmetics.edit")) list.addAll(Lists.newArrayList("bought", "setused", "sale"));
            return list;
        }

        if (sender.hasPermission("itzcosmetics.edit")) {
            switch (args[0]) {
                case "bought" -> {
                    ArrayList<String> list = new ArrayList<>();

                    if (args.length == 2) {
                        list.add("set");
                        list.add("unset");
                    }
                    else if (args.length == 3) {
                        list = getPlayers();
                        list.add("*");
                    }
                    else if (args.length == 4) {
                        for (Type type : plugin.getShopManager().getTypes()) {
                            list.add(type.getId());
                        }
                    }
                    else if (args.length == 5) {
                        Player player = Bukkit.getPlayer(args[2]);

                        if (player != null) {
                            switch (args[1]) {
                                case "set" -> {
                                    for (Cosmetics cosmetics : plugin.getShopManager().getCosmeticsByType(args[3])) {
                                        if (!plugin.getShopManager().isBought(player, cosmetics)) list.add(cosmetics.getId());
                                    }
                                }
                                case "unset" -> {
                                    for (Cosmetics cosmetics : plugin.getShopManager().getCosmeticsByType(args[2])) {
                                        if (plugin.getShopManager().isBought(player, cosmetics)) list.add(cosmetics.getId());
                                    }
                                }
                            }
                        }

                    }

                    return list;
                }

                case "setused" -> {
                    if (args.length == 2) {
                        ArrayList<String> list = getPlayers();
                        list.add("*");
                        return list;
                    }
                    if (args.length == 3) {
                        ArrayList<String> list = new ArrayList<>();
                        for (Type type : plugin.getShopManager().getTypes()) {
                            list.add(type.getId());
                        }
                        return list;
                    }
                    if (args.length == 4) {
                        ArrayList<String> list = new ArrayList<>();
                        Player player = Bukkit.getPlayer(args[1]);
                        if (player != null) {
                            for (Cosmetics cosmetics : plugin.getShopManager().getCosmeticsByType(args[2])) {
                                if (plugin.getShopManager().getUsed(player, args[2]) != cosmetics) list.add(cosmetics.getId());
                            }
                        }
                        return list;
                    }
                }

                case "sale" -> {
                    if (args.length == 2) return Lists.newArrayList("add", "remove", "list");

                    switch (args[1]) {
                        case "add" -> {
                            if (args.length == 3) return Lists.newArrayList("<id>");
                            if (args.length == 4) return Lists.newArrayList("exact", "after");
                            if (args.length == 5) return Lists.newArrayList("<yyyy.MM.dd;hh:mm:ss>");
                            if (args.length == 6) return Lists.newArrayList("<percent>");
                            if (args.length == 7) {
                                ArrayList<String> list = Lists.newArrayList("*");
                                for (Type type : plugin.getShopManager().getTypes()) {
                                    list.add(type.getId());
                                }
                                return list;
                            }
                            if (args.length == 8) {
                                ArrayList<String> list = Lists.newArrayList("*");
                                for (Rarity rarity : plugin.getShopManager().getRarities()) {
                                    list.add(rarity.getId());
                                }
                                return list;
                            }
                            if (args.length == 9) {
                                ArrayList<String> list = Lists.newArrayList("*");
                                for (Category category : plugin.getShopManager().getCategories()) {
                                    list.add(category.getId());
                                }
                                return list;
                            }
                            if (args.length == 10) {
                                ArrayList<String> list = Lists.newArrayList("*");
                                for (Cosmetics cosmetics : plugin.getShopManager().getCosmeticsByType(args[6])) {
                                    list.add(cosmetics.getId());
                                }
                                return list;
                            }
                        }
                        case "remove" -> {
                            if (args.length == 3) {
                                ArrayList<String> list = new ArrayList<>();
                                plugin.getShopManager().getSalesCache().forEach(stringSale -> {
                                    Sale sale = Sale.fromString(stringSale, plugin);
                                    if (sale != null) list.add(sale.getId());
                                });
                                return list;
                            }
                        }
                    }
                }
            }
        }

        return Lists.newArrayList();
    }

    private ArrayList<String> getPlayers() {
        ArrayList<String> players = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player -> players.add(player.getName()));
        return players;
    }
}