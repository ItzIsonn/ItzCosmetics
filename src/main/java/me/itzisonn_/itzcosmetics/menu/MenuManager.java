package me.itzisonn_.itzcosmetics.menu;

import lombok.Getter;
import me.itzisonn_.itzcosmetics.ItzCosmetics;
import me.itzisonn_.itzcosmetics.cosmetics.Category;
import me.itzisonn_.itzcosmetics.cosmetics.Cosmetics;
import me.itzisonn_.itzcosmetics.cosmetics.Rarity;
import me.itzisonn_.itzcosmetics.cosmetics.type.Type;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class MenuManager implements Listener {
    private final ItzCosmetics plugin;
    @Getter
    private final HashMap<Player, InventoryData> inventories = new HashMap<>();
    private Player queuePlayer;

    public MenuManager(ItzCosmetics plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, MenuType type) {
        InventoryData inventoryData;
        queuePlayer = player;

        if (inventories.get(player) != null) inventoryData = inventories.get(player);
        else inventoryData = new InventoryData(plugin);

        inventoryData.setMenuType(type);
        inventoryData.resetPage();
        inventoryData.updateInventory();
        inventories.put(player, inventoryData);
        queuePlayer = null;
    }

    public Player getPlayer(InventoryData data) {
        for (Player player : inventories.keySet()) {
            if (inventories.get(player) == data) return player;
        }

        return queuePlayer;
    }


    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onClick(InventoryClickEvent e) {
        if (plugin.getMenuManager().getInventories().get((Player) e.getWhoClicked()) == null || e.getClickedInventory() == null) return;

        if (e.isShiftClick() && e.getClickedInventory() == e.getWhoClicked().getInventory() &&
                plugin.isInventory(plugin.getMenuManager().getInventories().get((Player) e.getWhoClicked()).getInventory(), e.getWhoClicked().getOpenInventory().getTopInventory())) {
            e.setCancelled(true);
            return;
        }

        if (!plugin.isInventory(e.getClickedInventory(),
                plugin.getMenuManager().getInventories().get((Player) e.getWhoClicked()).getInventory())) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        List<String> functions = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.getNskFunctions(), PersistentDataType.LIST.strings());

        if (functions == null) return;

        Player player = (Player) e.getWhoClicked();
        InventoryData inventoryData = plugin.getMenuManager().getInventories().get(player);

        for (String function : functions) {
            String functionId = function.split("\\*")[0];
            String clickType = function.split("\\*")[1];

            if ((clickType.equals("LEFT") && !e.isLeftClick()) || (clickType.equals("RIGHT") && !e.isRightClick())) continue;

            switch (functionId.toUpperCase()) {
                case "NEXT_PAGE" -> inventoryData.nextPage();
                case "PREV_PAGE" -> inventoryData.prevPage();

                case "NEXT_SHOWTYPE" -> inventoryData.getFilter().nextShowType();
                case "PREV_SHOWTYPE" -> inventoryData.getFilter().prevShowType();

                case "NEXT_SORTTYPE" -> inventoryData.getFilter().nextSortType();
                case "PREV_SORTTYPE" -> inventoryData.getFilter().prevSortType();

                case "CONFIRM" -> {
                    Cosmetics cosmetics = inventoryData.getConfirming();

                    int cost = cosmetics.getRarity().getCost();
                    if (plugin.getShopManager().getSale(cosmetics) != null) cost = plugin.getShopManager().getSale(cosmetics).calculateCost(cosmetics);

                    String statusId = plugin.getUtils().getStatusId(player, cosmetics);

                    if (!plugin.getShopManager().isBought(player, cosmetics)) {
                        if (plugin.getShopManager().getMoney(player) >= cost) {
                            if (plugin.getShopManager().changeMoney(player, -cost)) {
                                plugin.getShopManager().setBought(player, cosmetics, true);
                            }
                            else {
                                plugin.getLogger().log(Level.SEVERE, "An error occurred when changing the player's balance (" + player.getName() + ")");
                            }
                        }
                    }

                    HashMap<String, Entity> entities = new HashMap<>();
                    entities.put("player", player);
                    for (String action : plugin.getConfigManager().getActions(statusId)) {
                        doAction(plugin.getUtils().parsePlaceholders(player,
                                plugin.getUtils().parseEntitiesPlaceholders(plugin.getUtils().parseCosmeticsPlaceholders(action, player, cosmetics), entities)));
                    }
                }

                case "CLOSE" -> player.closeInventory();

                default -> {
                    if (functionId.toUpperCase().matches("^COSMETICS:.*:.*")) {
                        Cosmetics cosmetics = plugin.getShopManager().getCosmetics(functionId.toUpperCase().split(":")[1], functionId.toUpperCase().split(":")[2]);

                        int cost = cosmetics.getRarity().getCost();
                        if (plugin.getShopManager().getSale(cosmetics) != null) cost = plugin.getShopManager().getSale(cosmetics).calculateCost(cosmetics);

                        String statusId = plugin.getUtils().getStatusId(player, cosmetics);

                        if (plugin.getShopManager().isBought(player, cosmetics)) {
                            if (plugin.getShopManager().getUsed(player, cosmetics.getType().getId()) != cosmetics) plugin.getShopManager().setUsed(player, cosmetics);
                        }
                        else {
                            if (plugin.getShopManager().getMoney(player) >= cost) {
                                if (plugin.getConfigManager().isConfirmMenuEnabled()) {
                                    inventoryData.setConfirming(cosmetics);
                                    open(player, MenuType.CONFIRM);
                                    continue;
                                }
                                else {
                                    if (plugin.getShopManager().changeMoney(player, -cost)) {
                                        plugin.getShopManager().setBought(player, cosmetics, true);
                                    }
                                    else {
                                        plugin.getLogger().log(Level.SEVERE, "An error occurred when changing the player's balance (" + player.getName() + ")");
                                    }
                                }
                            }
                        }

                        HashMap<String, Entity> entities = new HashMap<>();
                        entities.put("player", player);
                        for (String action : plugin.getConfigManager().getActions(statusId)) {
                            doAction(plugin.getUtils().parsePlaceholders(player,
                                    plugin.getUtils().parseEntitiesPlaceholders(plugin.getUtils().parseCosmeticsPlaceholders(action, player, cosmetics), entities)));
                        }
                    }

                    else if (functionId.toUpperCase().matches("^MENU:.*")) {
                        inventoryData.setMenuType(MenuType.valueOf(functionId.toUpperCase().split(":")[1]));
                        inventoryData.resetPage();
                    }

                    else if (functionId.toUpperCase().matches("^FILTER:.*:.*:.*")) {
                        String filterType = functionId.toUpperCase().split(":")[1];
                        String filterData = functionId.toUpperCase().split(":")[2];
                        String filterAction = functionId.split(":")[3].toLowerCase();

                        switch (filterType) {
                            case "TYPE" -> {
                                Type type = plugin.getShopManager().getType(filterData);

                                switch (filterAction) {
                                    case "true" -> {
                                        if (!inventoryData.getFilter().getTypes().contains(type)) inventoryData.getFilter().getTypes().add(type);
                                    }
                                    case "false" -> inventoryData.getFilter().getTypes().remove(type);
                                    case "toggle" -> {
                                        if (inventoryData.getFilter().getTypes().contains(type)) inventoryData.getFilter().getTypes().remove(type);
                                        else inventoryData.getFilter().getTypes().add(type);
                                    }
                                }
                            }
                            case "RARITY" -> {
                                Rarity rarity = plugin.getShopManager().getRarity(filterData);

                                switch (filterAction) {
                                    case "true" -> {
                                        if (!inventoryData.getFilter().getRarities().contains(rarity)) inventoryData.getFilter().getRarities().add(rarity);
                                    }
                                    case "false" -> inventoryData.getFilter().getRarities().remove(rarity);
                                    case "toggle" -> {
                                        if (inventoryData.getFilter().getRarities().contains(rarity)) inventoryData.getFilter().getRarities().remove(rarity);
                                        else inventoryData.getFilter().getRarities().add(rarity);
                                    }
                                }
                            }
                            case "CATEGORY" -> {
                                if (filterData.equals("WITHOUT")) {
                                    inventoryData.getFilter().setShowWithoutCategory(!inventoryData.getFilter().isShowWithoutCategory());
                                }

                                else {
                                    Category category = plugin.getShopManager().getCategory(filterData);

                                    switch (filterAction) {
                                        case "true" -> {
                                            if (!inventoryData.getFilter().getCategories().contains(category)) inventoryData.getFilter().getCategories().add(category);
                                        }
                                        case "false" -> inventoryData.getFilter().getCategories().remove(category);
                                        case "toggle" -> {
                                            if (inventoryData.getFilter().getCategories().contains(category)) inventoryData.getFilter().getCategories().remove(category);
                                            else inventoryData.getFilter().getCategories().add(category);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else if (functionId.toUpperCase().matches("^MESSAGE:.*")) {
                        String message = functionId.replaceAll("^MESSAGE:", "");
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                plugin.getUtils().parsePlaceholders(player, plugin.getUtils().parseMenuPlaceholders(inventoryData, message))));
                    }

                    else if (functionId.toUpperCase().matches("^PLAYER:.*")) {
                        String command = functionId.replaceAll("^PLAYER:", "");
                        player.chat("/" + plugin.getUtils().parsePlaceholders(player, plugin.getUtils().parseMenuPlaceholders(inventoryData, command)));
                    }

                    else if (functionId.toUpperCase().matches("^CONSOLE:.*")) {
                        String command = functionId.replaceAll("^CONSOLE:", "");
                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), plugin.getUtils().parsePlaceholders(player, plugin.getUtils().parseMenuPlaceholders(inventoryData, command)));
                    }
                }
            }
        }

        inventoryData.updateInventory();
    }

    private void doAction(String stringAction) {
        String target = stringAction.split("\\*")[0];
        String action = stringAction.replaceAll("^" + target + "\\*", "");
        new Thread(() -> plugin.getUtils().doAction(target, action)).start();
    }
}