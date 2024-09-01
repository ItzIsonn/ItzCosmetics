package me.itzisonn_.itzcosmetics;

import com.google.common.collect.Lists;
import me.itzisonn_.itzcosmetics.cosmetics.*;
import me.itzisonn_.itzcosmetics.cosmetics.Category;
import me.itzisonn_.itzcosmetics.cosmetics.Rarity;
import me.itzisonn_.itzcosmetics.cosmetics.type.ActivatorType;
import me.itzisonn_.itzcosmetics.cosmetics.type.Type;
import me.itzisonn_.itzcosmetics.menu.InventoryData;
import me.itzisonn_.itzcosmetics.menu.MenuType;
import me.itzisonn_.itzcosmetics.menu.filter.ShowType;
import me.itzisonn_.itzcosmetics.menu.filter.SortType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class ConfigManager {
    private final ItzCosmetics plugin;
    private FileConfiguration config;

    public ConfigManager(ItzCosmetics plugin) {
        this.plugin = plugin;

        reload();
    }



    public boolean isPapiEnabled() {
        return config.getBoolean("papi.enabled", true);
    }

    public boolean isPapiClearNameText() {
        return config.getBoolean("papi.clearNameText", false);
    }

    public String getCurrency() {
        String currency = config.getString("currency");

        if (currency == null) {
            plugin.getLogger().log(Level.SEVERE, "Can't find currency!");
            return null;
        }

        switch (currency.toUpperCase()) {
            case "PLAYERPOINTS" -> {
                return "PlayerPoints";
            }
            case "ESSENTIALS" -> {
                return "Essentials";
            }
            default -> {
                plugin.getLogger().log(Level.SEVERE, "Incorrect value for currency!");
                return null;
            }
        }
    }

    public boolean isRemovingNonExistentCosmetics() {
        return config.getBoolean("removeNonExistentCosmetics", false);
    }

    public int getSalesUpdateInterval() {
        return config.getInt("salesUpdateInterval", 200);
    }

    public String getDefaultByType(String type) {
        return config.getString("types." + type + ".default", "");
    }

    public int getCategoriesHideAmount() {
        return config.getInt("cosmeticsStyle.categories.hide.amount", 2);
    }

    public String getCategoriesHideReplace() {
        return config.getString("cosmeticsStyle.categories.hide.replace", "...");
    }

    public String getCategoriesEmpty() {
        return config.getString("cosmeticsStyle.categories.empty", "<red>Нет");
    }

    public String getCategoriesSeparator() {
        return config.getString("cosmeticsStyle.categories.separator", "<white>, ");
    }

    public String getExpiryTime(String time) {
        return config.getString("translation.expiryTime." + time, "?");
    }

    public String getPageName(InventoryData data) {
        return config.getString(data.getMenuType().getConfigName() + ".pages." + getPages(data).get(data.getPage()) + ".name", "?");
    }

    public List<String> getActions(String statusId) {
        return config.getStringList("cosmeticsStyle.actions." + statusId);
    }

    public String getComment(String statusId) {
        return config.getString("translation.comment." + statusId, "?");
    }

    public boolean isConfirmMenuEnabled() {
        return config.getBoolean("confirmMenu.enabled", true);
    }

    public String getMessage(String id) {
        return config.getString("translation.messages." + id, "?");
    }



    public ArrayList<Type> getTypes() {
        ArrayList<Type> types = new ArrayList<>();
        ConfigurationSection typesSection = config.getConfigurationSection("types");

        if (typesSection == null) {
            plugin.getLogger().log(Level.SEVERE, "Can't find types' section!");
            return types;
        }

        for (String id : typesSection.getKeys(false)) {
            ConfigurationSection section = typesSection.getConfigurationSection(id);
            if (section == null) continue;

            ActivatorType activatorType;

            if (section.getString("name") == null) {
                plugin.getLogger().log(Level.SEVERE, "Can't find name for type with id " + id + "! Skipping...");
                continue;
            }
            else if (section.getString("activator") == null) {
                plugin.getLogger().log(Level.SEVERE, "Can't find activator for type with id " + id + "! Skipping...");
                continue;
            }
            try {
                activatorType = ActivatorType.valueOf(section.getString("activator", "").split(":")[0].toUpperCase());
            }
            catch (IllegalArgumentException ignore) {
                plugin.getLogger().log(Level.SEVERE, "Unknown activator's type in type with id " + id + "! Skipping...");
                continue;
            }

            String activator = section.getString("activator", "");
            String[] activatorArgs = {};
            if (!activator.replaceAll("^" + activatorType, "").isBlank()) {
                activatorArgs = activator.replaceAll("^" + activatorType + ":", "").split(";");
            }

            switch (activatorType) {
                case KILL -> {
                    if (activatorArgs.length > 1) {
                        plugin.getLogger().log(Level.SEVERE, "Too many activator's args for activator's type " + activatorType + "! Skipping...");
                        continue;
                    }

                    if (!activatorArgs[0].equalsIgnoreCase("ENTITY") && !activatorArgs[0].equalsIgnoreCase("PLAYER")
                            && !activatorArgs[0].equalsIgnoreCase("ALL")) {
                        plugin.getLogger().log(Level.SEVERE, "Incorrect activator's args for activator's type " + activatorType + "! Skipping...");
                        continue;
                    }
                }

                case MOVE -> {
                    if (activatorArgs.length > 1) {
                        plugin.getLogger().log(Level.SEVERE, "Too many activator's args for activator's type " + activatorType + "! Skipping...");
                        continue;
                    }

                    if (!activatorArgs[0].equalsIgnoreCase("PLAYER") && !activatorArgs[0].equalsIgnoreCase("PROJECTILE")) {
                        plugin.getLogger().log(Level.SEVERE, "Incorrect activator's args for activator's type " + activatorType + "! Skipping...");
                        continue;
                    }
                }

                case REPEAT, ANIMATED_PLACEHOLDER -> {
                    try {
                        if (activatorArgs.length > 1) {
                            plugin.getLogger().log(Level.SEVERE, "Too many activator's args for activator's type " + activatorType + "! Skipping...");
                            continue;
                        }

                        int repeat = Integer.parseInt(activatorArgs[0]);

                        if (repeat < 1) {
                            plugin.getLogger().log(Level.SEVERE, "Incorrect activator's args for activator's type " + activatorType + "! Skipping...");
                            continue;
                        }
                    }
                    catch (NumberFormatException ignore) {
                        plugin.getLogger().log(Level.SEVERE, "Incorrect activator's args for activator's type " + activatorType + "! Skipping...");
                        continue;
                    }
                }

                case JOIN, QUIT, DEATH, RESPAWN, PLACEHOLDER, ON_USE -> {
                    if (activatorArgs.length >= 1) {
                        plugin.getLogger().log(Level.SEVERE, "Didn't expect any activator's args for activator's type " + activatorType + "! Skipping...");
                        continue;
                    }
                }
            }

            HashMap<String, ArrayList<String>> actions = new HashMap<>();
            ConfigurationSection actionsSection = section.getConfigurationSection("actions");
            if (actionsSection != null) {
                for (String actionGroup : actionsSection.getKeys(false)) {
                    actions.put(actionGroup, new ArrayList<>(actionsSection.getStringList(actionGroup)));
                }
            }

            types.add(new Type(
                    id,
                    section.getString("name"),
                    activatorType, activatorArgs,
                    actions));
        }

        return types;
    }

    public ArrayList<Rarity> getRarities() {
        ArrayList<Rarity> rarities = new ArrayList<>();
        ConfigurationSection raritiesSection = config.getConfigurationSection("rarities");

        if (raritiesSection == null) {
            plugin.getLogger().log(Level.SEVERE, "Can't find rarities section!");
            return rarities;
        }

        for (String id : raritiesSection.getKeys(false)) {
            ConfigurationSection section = raritiesSection.getConfigurationSection(id);
            if (section == null) continue;

            if (section.getString("name") == null) {
                plugin.getLogger().log(Level.SEVERE, "Can't find name for rarity with id " + id + "! Skipping...");
                continue;
            }
            else if (section.get("cost") == null) {
                plugin.getLogger().log(Level.SEVERE, "Can't find cost for rarity with id " + id + "! Skipping...");
                continue;
            }

            rarities.add(new Rarity(
                    id,
                    section.getInt("cost", 0),
                    section.getString("name")));
        }

        return rarities;
    }

    public ArrayList<Category> getCategories() {
        ArrayList<Category> categories = new ArrayList<>();
        ConfigurationSection categoriesSection = config.getConfigurationSection("categories");

        if (categoriesSection == null) {
            plugin.getLogger().log(Level.SEVERE, "Can't find categories section!");
            return categories;
        }

        for (String id : categoriesSection.getKeys(false)) {
            ConfigurationSection section = categoriesSection.getConfigurationSection(id);
            if (section == null) continue;

            if (section.getString("name") == null) {
                plugin.getLogger().log(Level.SEVERE, "Can't find name for category with id " + id + "! Skipping...");
                continue;
            }

            categories.add(new Category(
                    id,
                    section.getString("name")));
        }

        return categories;
    }

    public ArrayList<Cosmetics> getCosmetics() {
        ArrayList<Cosmetics> cosmeticsList = new ArrayList<>();
        ConfigurationSection cosmeticsSection = config.getConfigurationSection("cosmetics");

        if (cosmeticsSection == null) {
            plugin.getLogger().log(Level.SEVERE, "Can't find cosmetics section!");
            return cosmeticsList;
        }

        for (String typeId : cosmeticsSection.getKeys(false)) {
            if (plugin.getShopManager().getType(typeId) == null) {
                plugin.getLogger().log(Level.SEVERE, "Incorrect cosmetics' section's type id " + typeId + "! Skipping this type's section...");
                continue;
            }

            for (String id : Objects.requireNonNull(config.getConfigurationSection("cosmetics." + typeId)).getKeys(false)) {
                ConfigurationSection section = cosmeticsSection.getConfigurationSection(typeId + "." + id);
                if (section == null) continue;

                if (section.getString("name") == null) {
                    plugin.getLogger().log(Level.SEVERE, "Can't find name for cosmetics with id " + id + "! Skipping...");
                    continue;
                }
                else if (section.getString("material") == null) {
                    plugin.getLogger().log(Level.SEVERE, "Can't find material for cosmetics with id " + id + "! Skipping...");
                    continue;
                }
                else if (section.getString("rarity") == null) {
                    plugin.getLogger().log(Level.SEVERE, "Can't find rarity for cosmetics with id " + id + "! Skipping...");
                    continue;
                }
                try {
                    Material.valueOf(section.getString("material", "").toUpperCase());
                }
                catch (IllegalArgumentException ignore) {
                    plugin.getLogger().log(Level.SEVERE, "Unknown material in cosmetics with id " + id + "! Skipping...");
                    continue;
                }
                if (plugin.getShopManager().getRarity(section.getString("rarity")) == null) {
                    plugin.getLogger().log(Level.SEVERE, "Unknown rarity in cosmetics with id " + id + "! Skipping...");
                    continue;
                }

                ArrayList<Category> categories = new ArrayList<>();
                for (String categoryId : section.getStringList("categories")) {
                    categories.add(plugin.getShopManager().getCategory(categoryId));
                }

                HashMap<String, Object> data = new HashMap<>();
                ConfigurationSection dataSection = section.getConfigurationSection("data");
                if (dataSection != null) {
                    for (String dataId : dataSection.getKeys(false)) {
                        data.put(dataId, section.get("data." + dataId, ""));
                    }
                }

                cosmeticsList.add(new Cosmetics(
                        id,
                        section.getString("name"),
                        Material.valueOf(section.getString("material", "").toUpperCase()),
                        plugin.getShopManager().getType(typeId),
                        plugin.getShopManager().getRarity(section.getString("rarity")),
                        categories,
                        new ArrayList<>(section.getStringList("description")),
                        data));
            }
        }

        return cosmeticsList;
    }



    public ArrayList<String> getPages(InventoryData data) {
        if (data.getMenuType() == MenuType.COSMETICS) {
            ArrayList<Integer> slots = new ArrayList<>();

            for (String string : config.getStringList("cosmeticsMenu.cosmeticsSlots")) {
                try {
                    slots.add(Integer.parseInt(string));
                }
                catch (NumberFormatException ignored) {
                    try {
                        for (int i = Integer.parseInt(string.split("-")[0]); i <= Integer.parseInt(string.split("-")[1]); i++) {
                            slots.add(i);
                        }
                    }
                    catch (NumberFormatException ignored1) {
                        plugin.getLogger().log(Level.SEVERE, "Incorrect slots format for " + data.getMenuType().getConfigName() + "!");
                        return new ArrayList<>();
                    }
                }
            }

            ArrayList<String> pages = new ArrayList<>();
            int cosmetics = plugin.getShopManager().getCosmeticsList().size();
            int num = 1;
            while (cosmetics > 0) {
                pages.add(String.valueOf(num));
                num++;
                cosmetics -= slots.size();
            }

            return pages;
        }

        else if (data.getMenuType() == MenuType.FILTER) {
            ConfigurationSection section = config.getConfigurationSection("filterMenu.pages");
            if (section == null) {
                plugin.getLogger().log(Level.SEVERE, "Can't find pages for " + data.getMenuType().getConfigName() + "!");
                return new ArrayList<>();
            }
            return new ArrayList<>(section.getKeys(false));
        }

        return new ArrayList<>();
    }

    public @NotNull Inventory getInventory(InventoryData data) {
        int size = 54;
        String title = "";
        if (config.get(data.getMenuType().getConfigName() + ".size") == null) {
            plugin.getLogger().log(Level.SEVERE, "Can't find size for " + data.getMenuType().getConfigName() + "! Setting default value...");
        }
        else if (config.getInt(data.getMenuType().getConfigName() + ".size") % 9 != 0) {
            plugin.getLogger().log(Level.SEVERE, "Size for " + data.getMenuType().getConfigName() + " must be multiple of 9! Setting default value...");
        }
        else {
            size = config.getInt(data.getMenuType().getConfigName() + ".size");
        }

        if (config.getString(data.getMenuType().getConfigName() + ".title") == null) {
            plugin.getLogger().log(Level.SEVERE, "Can't find title for " + data.getMenuType().getConfigName() + "! Setting empty value...");
        }
        else {
            title = config.getString(data.getMenuType().getConfigName() + ".title", "");
        }

        Inventory inventory = Bukkit.createInventory(data, size, MiniMessage.miniMessage().deserialize(
                plugin.getUtils().parsePlaceholders(plugin.getMenuManager().getPlayer(data), plugin.getUtils().parseMenuPlaceholders(data, title))));
        ConfigurationSection section = config.getConfigurationSection(data.getMenuType().getConfigName() + ".items");

        if (section == null) {
            plugin.getLogger().log(Level.SEVERE, "Can't find items section for " + data.getMenuType().getConfigName() + "!");
            return inventory;
        }

        for (String itemId : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(itemId);
            if (itemSection == null) continue;

            ArrayList<Integer> slots = new ArrayList<>();
            try {
                slots.add(Integer.parseInt(itemSection.getString("slots", "?")));
            }
            catch (NumberFormatException ignore) {
                for (String string : itemSection.getStringList("slots")) {
                    try {
                        slots.add(Integer.parseInt(string));
                    }
                    catch (NumberFormatException ignored) {
                        try {
                            for (int i = Integer.parseInt(string.split("-")[0]); i <= Integer.parseInt(string.split("-")[1]); i++) {
                                slots.add(i);
                            }
                        }
                        catch (NumberFormatException ignored1) {
                            plugin.getLogger().log(Level.SEVERE, "Incorrect slots format for " + data.getMenuType().getConfigName() + "!");
                            return inventory;
                        }
                    }
                }
            }

            ItemStack item = getItem(data, itemSection);
            if (item != null) {
                for (int i : slots) {
                    inventory.setItem(i, item);
                }
            }
        }

        return inventory;
    }

    public void placeItems(InventoryData data) {
        Player player = plugin.getMenuManager().getPlayer(data);

        if (data.getMenuType() == MenuType.COSMETICS) {
            ArrayList<Cosmetics> unsortedCosmeticsList = data.getFilter().getFilteredCosmetics();

            ArrayList<Integer> slots = new ArrayList<>();
            for (String string : config.getStringList("cosmeticsMenu.cosmeticsSlots")) {
                try {
                    slots.add(Integer.parseInt(string));
                }
                catch (NumberFormatException ignored) {
                    try {
                        for (int i = Integer.parseInt(string.split("-")[0]); i <= Integer.parseInt(string.split("-")[1]); i++) {
                            slots.add(i);
                        }
                    }
                    catch (NumberFormatException ignored1) {
                        plugin.getLogger().log(Level.SEVERE, "Incorrect slots format for " + data.getMenuType().getConfigName() + "!");
                        return;
                    }
                }
            }

            ArrayList<Cosmetics> cosmeticsList = new ArrayList<>();

            for (int i = slots.size() * data.getPage(); i < slots.size() * (data.getPage() + 1); i++) {
                if (i >= unsortedCosmeticsList.size()) break;
                cosmeticsList.add(unsortedCosmeticsList.get(i));
            }

            for (int i : slots) {
                if (cosmeticsList.isEmpty()) return;

                Cosmetics cosmetics = cosmeticsList.get(0);
                cosmeticsList.remove(cosmetics);

                ItemStack item = new ItemStack(cosmetics.getItem());
                ItemMeta itemMeta = item.getItemMeta();

                if (plugin.getShopManager().isBought(player, cosmetics) && plugin.getShopManager().getUsed(player, cosmetics.getType().getId()) == cosmetics &&
                        config.getBoolean("glowWhenUse", true)) itemMeta.addEnchant(Enchantment.THORNS, 1, false);

                ArrayList<String> template = new ArrayList<>(config.getStringList("cosmeticsStyle.template"));
                if (plugin.getShopManager().getSale(cosmetics) != null) template = new ArrayList<>(config.getStringList("cosmeticsStyle.saleTemplate"));

                itemMeta.displayName(MiniMessage.miniMessage().deserialize("<white><i:false>" +
                        plugin.getUtils().parsePlaceholders(player,
                                plugin.getUtils().parseMenuPlaceholders(data, plugin.getUtils().parseCosmeticsPlaceholders(template.get(0), player, cosmetics)))));

                template.remove(0);

                ArrayList<String> lore = new ArrayList<>();
                for (String string : template) {
                    if (string.contains("%description%")) {
                        if (cosmetics.getDescription().isEmpty()) {
                            lore.add(plugin.getUtils().parseCosmeticsPlaceholders(string.replace("%description%", ""), player, cosmetics));
                        }
                        else {
                            for (String description : cosmetics.getDescription()) {
                                lore.add(plugin.getUtils().parseCosmeticsPlaceholders(string.replace("%description%", description), player, cosmetics));
                            }
                        }
                    }
                    else lore.add(plugin.getUtils().parsePlaceholders(player, plugin.getUtils().parseCosmeticsPlaceholders(string, player, cosmetics)));
                }
                itemMeta.lore(convertStringList(data, lore));

                itemMeta.getPersistentDataContainer().set(
                        plugin.getNskFunctions(),
                        PersistentDataType.LIST.strings(),
                        Lists.newArrayList("COSMETICS:" + cosmetics.getType().getId() + ":" + cosmetics.getId() + "*ALL"));


                itemMeta.addItemFlags(ItemFlag.values());
                itemMeta.setAttributeModifiers(null);
                item.setItemMeta(itemMeta);

                data.getInventory().setItem(i, item);
            }
        }

        else if (data.getMenuType() == MenuType.FILTER) {
            ConfigurationSection section = config.getConfigurationSection("filterMenu.pages." + getPages(data).get(data.getPage()) + ".items");

            if (section == null) {
                plugin.getLogger().log(Level.SEVERE, "Can't find page's items for " + data.getMenuType().getConfigName() + " (page " + getPages(data).get(data.getPage()) + ")!");
                return;
            }

            for (String itemId : section.getKeys(false)) {
                ConfigurationSection itemSection = section.getConfigurationSection(itemId);
                if (itemSection == null) continue;

                ArrayList<Integer> slots = new ArrayList<>();
                try {
                    slots.add(Integer.parseInt(itemSection.getString("slots", "?")));
                }
                catch (NumberFormatException ignore) {
                    for (String string : itemSection.getStringList("slots")) {
                        try {
                            slots.add(Integer.parseInt(string));
                        }
                        catch (NumberFormatException ignored) {
                            try {
                                for (int i = Integer.parseInt(string.split("-")[0]); i <= Integer.parseInt(string.split("-")[1]); i++) {
                                    slots.add(i);
                                }
                            }
                            catch (NumberFormatException ignored1) {
                                plugin.getLogger().log(Level.SEVERE, "Incorrect slots format for " + data.getMenuType().getConfigName() + "!");
                                return;
                            }
                        }
                    }
                }

                ItemStack item = getItem(data, itemSection);
                if (item != null) {
                    for (int i : slots) {
                        data.getInventory().setItem(i, item);
                    }
                }
            }
        }

        else if (data.getMenuType() == MenuType.CONFIRM) {
            ConfigurationSection section = config.getConfigurationSection("confirmMenu.items");

            if (section == null) {
                plugin.getLogger().log(Level.SEVERE, "Can't find items for " + data.getMenuType().getConfigName() + "!");
                return;
            }

            for (String itemId : section.getKeys(false)) {
                ConfigurationSection itemSection = section.getConfigurationSection(itemId);
                if (itemSection == null) continue;

                ArrayList<Integer> slots = new ArrayList<>();
                try {
                    slots.add(Integer.parseInt(itemSection.getString("slots", "?")));
                }
                catch (NumberFormatException ignore) {
                    for (String string : itemSection.getStringList("slots")) {
                        try {
                            slots.add(Integer.parseInt(string));
                        }
                        catch (NumberFormatException ignored) {
                            try {
                                for (int i = Integer.parseInt(string.split("-")[0]); i <= Integer.parseInt(string.split("-")[1]); i++) {
                                    slots.add(i);
                                }
                            }
                            catch (NumberFormatException ignored1) {
                                plugin.getLogger().log(Level.SEVERE, "Incorrect slots format for " + data.getMenuType().getConfigName() + "!");
                                return;
                            }
                        }
                    }
                }

                ItemStack item = getItem(data, itemSection);
                Cosmetics cosmetics = data.getConfirming();
                if (item != null) {
                    ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.displayName(MiniMessage.miniMessage().deserialize("<white><i:false>" +
                            plugin.getUtils().parsePlaceholders(player, plugin.getUtils().parseMenuPlaceholders(data, plugin.getUtils().parseCosmeticsPlaceholders(
                                    itemSection.getString("name", ""),
                                    player, cosmetics)))));


                    ArrayList<String> lore = new ArrayList<>();
                    for (String string : itemSection.getStringList("lore")) {
                        if (string.contains("%description%")) {
                            if (cosmetics.getDescription().isEmpty()) {
                                lore.add(plugin.getUtils().parseCosmeticsPlaceholders(string.replace("%description%", ""), player, cosmetics));
                            }
                            else {
                                for (String description : cosmetics.getDescription()) {
                                    lore.add(plugin.getUtils().parseCosmeticsPlaceholders(string.replace("%description%", description), player, cosmetics));
                                }
                            }
                        }
                        else lore.add(plugin.getUtils().parseCosmeticsPlaceholders(string, player, cosmetics));
                    }
                    itemMeta.lore(convertStringList(data, lore));
                    item.setItemMeta(itemMeta);

                    for (int i : slots) {
                        data.getInventory().setItem(i, item);
                    }
                }
            }
        }
    }

    public ItemStack getItem(InventoryData data, ConfigurationSection section) {
        if (section.getString("material") == null) {
            plugin.getLogger().log(Level.SEVERE, "Incorrect item format for " + data.getMenuType().getConfigName() + " (section " + section.getName() + ")! Skipping...");
            return null;
        }
        try {
            Material.valueOf(section.getString("material", "").toUpperCase());
        }
        catch (IllegalArgumentException ignore) {
            plugin.getLogger().log(Level.SEVERE, "Incorrect item format for " + data.getMenuType().getConfigName() + " (section " + section.getName() + ")! Skipping...");
            return null;
        }

        ItemStack item = new ItemStack(Material.valueOf(section.getString("material", "").toUpperCase()));
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.addItemFlags(ItemFlag.values());
        itemMeta.setAttributeModifiers(null);

        itemMeta.displayName(MiniMessage.miniMessage().deserialize("<white><i:false>" +
                plugin.getUtils().parsePlaceholders(plugin.getMenuManager().getPlayer(data), plugin.getUtils().parseMenuPlaceholders(data, section.getString("name", "")))));
        itemMeta.lore(convertStringList(data, section.getStringList("lore")));

        if (section.getBoolean("enchanted", false)) itemMeta.addEnchant(Enchantment.THORNS, 1, true);
        item.setAmount(section.getInt("amount", 1));

        if (section.getConfigurationSection("functions") != null) {
            List<String> functions = new ArrayList<>();
            ConfigurationSection functionsSection = section.getConfigurationSection("functions");

            if (functionsSection == null) {
                item.setItemMeta(itemMeta);
                return item;
            }

            for (String functionId : functionsSection.getKeys(false)) {
                String clickType = functionsSection.getString(functionId, "").toUpperCase();
                String upperFunctionId = functionId.toUpperCase();
                if (!upperFunctionId.equalsIgnoreCase("NEXT_PAGE") && !upperFunctionId.equalsIgnoreCase("PREV_PAGE") &&
                        !upperFunctionId.equalsIgnoreCase("NEXT_SHOWTYPE") && !upperFunctionId.equalsIgnoreCase("PREV_SHOWTYPE") &&
                        !upperFunctionId.equalsIgnoreCase("NEXT_SORTTYPE") && !upperFunctionId.equalsIgnoreCase("PREV_SORTTYPE") &&
                        !upperFunctionId.equalsIgnoreCase("CONFIRM") && !upperFunctionId.equalsIgnoreCase("CLOSE") &&
                        !upperFunctionId.matches("^COSMETICS:.*:.*") && !upperFunctionId.matches("^MENU:.*") &&
                        !upperFunctionId.matches("^FILTER:.*:.*:.*") && !upperFunctionId.matches("^MESSAGE:.*") &&
                        !upperFunctionId.matches("^PLAYER:.*") && !upperFunctionId.matches("^CONSOLE:.*")) {
                    plugin.getLogger().log(Level.SEVERE, "Incorrect function id" + functionId + " (section " + section.getName() + ")! Skipping...");
                }
                else if (clickType.equals("LEFT") || clickType.equals("RIGHT") || clickType.equals("ALL")) {
                    functions.add(functionId + "*" + clickType);
                }
                else {
                    plugin.getLogger().log(Level.SEVERE, "Incorrect click type for function id" + functionId + " (section " + section.getName() + ")! Skipping...");
                }
            }
            itemMeta.getPersistentDataContainer().set(plugin.getNskFunctions(), PersistentDataType.LIST.strings(), functions);
        }

        item.setItemMeta(itemMeta);
        return item;
    }



    public String getStatusTranslation(boolean isOn) {
        return config.getString("translation.status." + isOn, "?");
    }

    public String getShowTypeTranslation(ShowType type) {
        return config.getString("translation.showType." + type.getConfigName(), "?");
    }

    public String getSortTypeTranslation(SortType type) {
        return config.getString("translation.sortType." + type.getConfigName(), "?");
    }



    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }



    private List<Component> convertStringList(InventoryData data, List<String> strings) {
        List<Component> components = Lists.newArrayList();

        for (String string : strings) {
            components.add(MiniMessage.miniMessage().deserialize("<white><i:false>" +
                    plugin.getUtils().parsePlaceholders(plugin.getMenuManager().getPlayer(data), plugin.getUtils().parseMenuPlaceholders(data, string))));
        }

        return components;
    }
}