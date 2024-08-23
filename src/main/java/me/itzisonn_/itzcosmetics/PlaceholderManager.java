package me.itzisonn_.itzcosmetics;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.itzisonn_.itzcosmetics.cosmetics.Cosmetics;
import me.itzisonn_.itzcosmetics.cosmetics.type.ActivatorType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderManager extends PlaceholderExpansion {
    private final ItzCosmetics plugin;

    public PlaceholderManager(ItzCosmetics plugin) {
        this.plugin = plugin;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        String keyword = params.split("_")[0].toLowerCase();
        String data = "";
        if (!params.equals(keyword)) data = params.replaceFirst(keyword + "_", "");

        switch (keyword) {
            case "placeholder" -> {
                if (player == null) return null;

                Cosmetics cosmetics = plugin.getShopManager().getUsed(player, data);
                if (cosmetics == null) return null;

                if (cosmetics.getType().getActivatorType() == ActivatorType.PLACEHOLDER) {
                    if (cosmetics.getData().get("text") == null) return "";
                    return PlaceholderAPI.setPlaceholders(player, cosmetics.getData().get("text").toString());
                }
                if (cosmetics.getType().getActivatorType() == ActivatorType.ANIMATED_PLACEHOLDER) {
                    return PlaceholderAPI.setPlaceholders(player, plugin.getCosmeticsManager().getPlaceholder(cosmetics));
                }
            }

            case "bought" -> {
                if (player == null) return null;

                int bought = 0;

                if (data.isEmpty()) {
                    for (Cosmetics cosmetics : plugin.getShopManager().getCosmeticsList()) {
                        if (plugin.getShopManager().isBought(player, cosmetics)) bought++;
                    }
                }
                else {
                    if (plugin.getShopManager().getType(data) == null) return null;

                    for (Cosmetics cosmetics : plugin.getShopManager().getCosmeticsByType(data)) {
                        if (plugin.getShopManager().isBought(player, cosmetics)) bought++;
                    }
                }

                return String.valueOf(bought);
            }

            case "used" -> {
                if (player == null) return null;

                if (plugin.getShopManager().getType(data) == null) return null;

                String name = plugin.getShopManager().getUsed(player, data).getName();
                if (plugin.getConfigManager().isPapiClearNameText()) name = MiniMessage.miniMessage().stripTags(name);
                return PlaceholderAPI.setPlaceholders(player, name);
            }

            case "total" -> {
                if (data.isEmpty()) {
                    return String.valueOf(plugin.getShopManager().getCosmeticsList().size());
                }
                else {
                    if (plugin.getShopManager().getType(data) != null) return String.valueOf(plugin.getShopManager().getCosmeticsByType(data).size());
                }
            }
        }

        return null;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cosmetics";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ItzIsonn_";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }
}
