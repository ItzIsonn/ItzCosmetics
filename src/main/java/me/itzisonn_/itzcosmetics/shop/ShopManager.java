package me.itzisonn_.itzcosmetics.shop;

import lombok.Getter;
import lombok.Setter;
import me.itzisonn_.itzcosmetics.ItzCosmetics;
import me.itzisonn_.itzcosmetics.cosmetics.*;
import me.itzisonn_.itzcosmetics.cosmetics.Category;
import me.itzisonn_.itzcosmetics.cosmetics.Rarity;
import me.itzisonn_.itzcosmetics.cosmetics.type.ActivatorType;
import me.itzisonn_.itzcosmetics.cosmetics.type.Type;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

public class ShopManager {
    private final ItzCosmetics plugin;
    private final String currency;
    @Getter @Setter
    private ArrayList<Type> types = new ArrayList<>();
    @Getter @Setter
    private ArrayList<Rarity> rarities = new ArrayList<>();
    @Getter @Setter
    private ArrayList<Category> categories = new ArrayList<>();
    @Getter @Setter
    private ArrayList<Cosmetics> cosmeticsList = new ArrayList<>();

    private final HashMap<String, ArrayList<String>> boughtCache = new HashMap<>();
    private final HashMap<String, ArrayList<String>> usedCache = new HashMap<>();
    @Getter
    private ArrayList<String> salesCache = new ArrayList<>();



    public ShopManager(ItzCosmetics plugin) {
        this.plugin = plugin;

        if (plugin.getConfigManager().getCurrency() == null) {
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            currency = null;
            return;
        }

        if (!Bukkit.getServer().getPluginManager().isPluginEnabled(plugin.getConfigManager().getCurrency())) {
            plugin.getLogger().log(Level.SEVERE, "Can't find currency plugin! Disabling...");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            currency = null;
            return;
        }

        currency = plugin.getConfigManager().getCurrency();
    }



    public boolean changeMoney(Player player, int change) {
        if (currency.equals("Essentials")) {
            EssentialsHook.changeMoney(player, change);
            return true;
        }

        else if (currency.equals("PlayerPoints")) {
            PlayerPointsHook.changeMoney(player, change);
            return true;
        }

        else return false;
    }

    public int getMoney(Player player) {
        if (currency.equals("Essentials")) return EssentialsHook.getMoney(player);
        else if (currency.equals("PlayerPoints")) return PlayerPointsHook.getMoney(player);
        else return 0;
    }

    public boolean isBought(Player player, Cosmetics cosmetics) {
        for (String string : boughtCache.get(player.getName())) {
            if (string.equals(cosmetics.toString())) return true;
        }

        return false;
    }

    public void setBought(Player player, Cosmetics cosmetics, boolean b) {
        ArrayList<String> bought = new ArrayList<>(boughtCache.get(player.getName()));

        if (!b) {
            bought.remove(cosmetics.toString());
            setUsed(player, getCosmetics(cosmetics.getType().getId(), plugin.getConfigManager().getDefaultByType(cosmetics.getType().getId())));
        }
        else if (!bought.contains(cosmetics.toString())) bought.add(cosmetics.toString());

        if (!bought.equals(boughtCache.get(player.getName()))) {
            plugin.getDatabaseManager().setBought(player.getName(), bought);
            boughtCache.put(player.getName(), bought);
        }
    }

    public Cosmetics getUsed(Player player, String typeId) {
        for (String string : usedCache.get(player.getName())) {
            if (string.matches(typeId + ":.*")) {
                return getCosmetics(typeId, string.replaceAll("^" + typeId + ":", ""));
            }
        }

        return null;
    }

    public void setUsed(Player player, Cosmetics cosmetics) {
        ArrayList<String> used = new ArrayList<>(usedCache.get(player.getName()));

        used.removeIf(string -> string.matches(cosmetics.getType().getId() + ":.*"));
        used.add(cosmetics.toString());

        if (!used.equals(usedCache.get(player.getName()))) {
            plugin.getDatabaseManager().setUsed(player.getName(), used);
            usedCache.put(player.getName(), used);
            plugin.getCosmeticsManager().onUse(player, cosmetics);
        }
    }

    public void addSale(Sale sale) {
        ArrayList<String> sales = new ArrayList<>(salesCache);
        sales.add(sale.toString());
        if (!sales.equals(salesCache)) {
            plugin.getDatabaseManager().setSales(sales);
            salesCache = sales;
        }
    }

    public void removeSale(String id) {
        ArrayList<String> sales = new ArrayList<>(salesCache);
        sales.remove(getSale(id).toString());
        if (!sales.equals(salesCache)) {
            plugin.getDatabaseManager().setSales(sales);
            salesCache = sales;
        }
    }

    public void checkSales() {
        ArrayList<String> sales = new ArrayList<>(salesCache);

        Iterator<String> iterator = sales.iterator();

        while (iterator.hasNext()) {
            String stringSale = iterator.next();
            Sale sale = Sale.fromString(stringSale, plugin);

            if (sale == null) {
                iterator.remove();
                continue;
            }

            if (sale.getExpireDate().isBefore(LocalDateTime.now())) {
                iterator.remove();
            }
        }

        if (!sales.equals(salesCache)) {
            plugin.getDatabaseManager().setSales(sales);
            salesCache = sales;
        }
    }

    public void updateData() {
        types.clear();
        types.addAll(plugin.getConfigManager().getTypes());
        rarities.clear();
        rarities.addAll(plugin.getConfigManager().getRarities());
        categories.clear();
        categories.addAll(plugin.getConfigManager().getCategories());
        cosmeticsList.clear();
        cosmeticsList.addAll(plugin.getConfigManager().getCosmetics());

        if (plugin.getConfigManager().isRemovingNonExistentCosmetics()) {
            HashMap<String, ArrayList<String>> bought = new HashMap<>(boughtCache);
            HashMap<String, ArrayList<String>> used = new HashMap<>(usedCache);

            for (String player : bought.keySet()) {
                ArrayList<String> playerBought = new ArrayList<>(bought.get(player));
                playerBought.removeIf(cosmetics -> getCosmetics(cosmetics.split(":")[0], cosmetics.split(":")[1]) == null);

                if (!playerBought.equals(bought.get(player))) {
                    plugin.getDatabaseManager().setBought(player, playerBought);
                    boughtCache.put(player, playerBought);
                }
            }

            for (String player : used.keySet()) {
                ArrayList<String> playerUsed = new ArrayList<>(used.get(player));
                playerUsed.removeIf(cosmetics -> getCosmetics(cosmetics.split(":")[0], cosmetics.split(":")[1]) == null);

                if (!playerUsed.equals(used.get(player))) {
                    plugin.getDatabaseManager().setUsed(player, playerUsed);
                    usedCache.put(player, playerUsed);
                }
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            updateCosmetics(player);
        }
    }

    public Sale getSale(Cosmetics cosmetics) {
        for (String stringSale : salesCache) {
            Sale sale = Sale.fromString(stringSale, plugin);

            if (sale == null) continue;

            if (sale.hasSale(cosmetics)) {
                return sale;
            }
        }

        return null;
    }

    public Sale getSale(String id) {
        for (String stringSale : salesCache) {
            Sale sale = Sale.fromString(stringSale, plugin);

            if (sale == null) continue;

            if (sale.getId().equalsIgnoreCase(id)) {
                return sale;
            }
        }

        return null;
    }

    public Type getType(String id) {
        for (Type type : types) {
            if (id.equalsIgnoreCase(type.getId())) return type;
        }
        return null;
    }

    public Rarity getRarity(String id) {
        for (Rarity rarity : rarities) {
            if (id.equalsIgnoreCase(rarity.getId())) return rarity;
        }
        return null;
    }

    public Category getCategory(String id) {
        for (Category category : categories) {
            if (id.equalsIgnoreCase(category.getId())) return category;
        }
        return null;
    }

    public Cosmetics getCosmetics(String type, String id) {
        for (Cosmetics cosmetics : cosmeticsList) {
            if (type.equalsIgnoreCase(cosmetics.getType().getId()) && id.equalsIgnoreCase(cosmetics.getId())) return cosmetics;
        }

        return null;
    }

    public ArrayList<Cosmetics> getCosmeticsByType(String typeId) {
        ArrayList<Cosmetics> returnList = new ArrayList<>();

        for (Cosmetics cosmetics : cosmeticsList) {
            if (cosmetics.getType().getId().equalsIgnoreCase(typeId)) returnList.add(cosmetics);
        }

        return returnList;
    }

    public ArrayList<Cosmetics> getCosmeticsByActivatorType(ActivatorType type) {
        ArrayList<Cosmetics> returnList = new ArrayList<>();

        for (Cosmetics cosmetics : cosmeticsList) {
            if (cosmetics.getType().getActivatorType() == type) returnList.add(cosmetics);
        }

        return returnList;
    }

    public void updateCosmetics(Player player) {
        if (!plugin.getDatabaseManager().hasData(player.getName())) {
            plugin.getDatabaseManager().setBought(player.getName(), new ArrayList<>());
            plugin.getDatabaseManager().setUsed(player.getName(), new ArrayList<>());
        }

        boughtCache.computeIfAbsent(player.getName(), k -> plugin.getDatabaseManager().getBought(player.getName()));
        usedCache.computeIfAbsent(player.getName(), k -> plugin.getDatabaseManager().getUsed(player.getName()));

        for (Type type : getTypes()) {
            Cosmetics cosmetics = getCosmetics(type.getId(), plugin.getConfigManager().getDefaultByType(type.getId()));

            if (cosmetics != null) {
                if (!isBought(player, cosmetics)) setBought(player, cosmetics, true);
                if (getUsed(player, type.getId()) == null) setUsed(player, cosmetics);
            }
        }
    }

    public void loadSales() {
        salesCache = plugin.getDatabaseManager().getSales();
        checkSales();
    }
}