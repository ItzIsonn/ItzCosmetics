package me.itzisonn_.itzcosmetics.menu;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.itzisonn_.itzcosmetics.ItzCosmetics;
import me.itzisonn_.itzcosmetics.cosmetics.Cosmetics;
import me.itzisonn_.itzcosmetics.menu.filter.Filter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@EqualsAndHashCode
public class InventoryData implements InventoryHolder {
    private final ItzCosmetics plugin;
    @Getter
    private Inventory inventory;
    @Getter
    private final Filter filter;
    @Getter @Setter
    private MenuType menuType;
    @Getter
    private int page = 0;
    @Getter @Setter
    private Cosmetics confirming;

    public InventoryData(ItzCosmetics plugin) {
        this.plugin = plugin;
        filter = new Filter(plugin, this);
    }

    public void updateInventory() {
        inventory = plugin.getConfigManager().getInventory(this);
        plugin.getConfigManager().placeItems(this);
        plugin.getMenuManager().getPlayer(this).openInventory(inventory);
    }

    public void nextPage() {
        if (page < plugin.getConfigManager().getPages(this).size() - 1) page++;
    }

    public void prevPage() {
        if (page > 0) page--;
    }

    public void resetPage() {
        page = 0;
    }
}