package me.itzisonn_.itzcosmetics;

import lombok.Getter;
import me.itzisonn_.itzcosmetics.command.Command;
import me.itzisonn_.itzcosmetics.command.TabCompleter;
import me.itzisonn_.itzcosmetics.cosmetics.CosmeticsManager;
import me.itzisonn_.itzcosmetics.database.DatabaseManager;
import me.itzisonn_.itzcosmetics.database.SQLiteConnector;
import me.itzisonn_.itzcosmetics.menu.MenuManager;
import me.itzisonn_.itzcosmetics.menu.InventoryData;
import me.itzisonn_.itzcosmetics.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.logging.Level;

@Getter
public class ItzCosmetics extends JavaPlugin {
    private ConfigManager configManager;
    private ShopManager shopManager;
    private DatabaseManager databaseManager;
    private MenuManager menuManager;
    private Utils utils;
    private CosmeticsManager cosmeticsManager;
    private boolean isHookedPapi = false;
    private final ArrayList<Integer> taskIds = new ArrayList<>();
    private NamespacedKey nskFunctions;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        nskFunctions = new NamespacedKey(this, "functions");
        configManager = new ConfigManager(this);
        isHookedPapi = configManager.isPapiEnabled() && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        shopManager = new ShopManager(this);
        databaseManager = new SQLiteConnector(this);
        databaseManager.load();
        menuManager = new MenuManager(this);
        utils = new Utils(this);
        cosmeticsManager = new CosmeticsManager(this);

        new Command(this);
        new TabCompleter(this);

        shopManager.loadSales();
        reload();

        Bukkit.getServer().getPluginManager().registerEvents(menuManager, this);
        Bukkit.getServer().getPluginManager().registerEvents(cosmeticsManager, this);

        if (configManager.isPapiEnabled()) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                getLogger().log(Level.INFO, "Successfully hooked into PlaceholderAPI!");
                new PlaceholderManager(this).register();
            }
            else {
                getLogger().log(Level.SEVERE, "Can't find PlaceholderAPI plugin!");
            }
        }

        getLogger().log(Level.INFO, "Enabled!");
    }

    @Override
    public void onDisable() {
        for (int taskId : taskIds) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        getLogger().log(Level.INFO, "Disabled!");
    }

    public void reload() {
        for (int taskId : taskIds) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        configManager.reload();

        shopManager.updateData();

        for (InventoryData inventoryData : menuManager.getInventories().values()) {
            inventoryData.getFilter().updateData();
        }

        taskIds.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> cosmeticsManager.onProjectileMove(), 0, 1));
        taskIds.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> shopManager.checkSales(), 0, configManager.getSalesUpdateInterval()));
        cosmeticsManager.startPlaceholderUpdate();
        cosmeticsManager.startRepeat();

        getLogger().log(Level.INFO, "Reloaded!");
    }



    public boolean isInventory(Inventory inv1, Inventory inv2) {
        return inv1.getHolder() == inv2.getHolder();
    }
}