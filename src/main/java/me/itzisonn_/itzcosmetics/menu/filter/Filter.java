package me.itzisonn_.itzcosmetics.menu.filter;

import lombok.Getter;
import lombok.Setter;
import me.itzisonn_.itzcosmetics.ItzCosmetics;
import me.itzisonn_.itzcosmetics.cosmetics.Cosmetics;
import me.itzisonn_.itzcosmetics.cosmetics.Category;
import me.itzisonn_.itzcosmetics.cosmetics.Rarity;
import me.itzisonn_.itzcosmetics.cosmetics.type.Type;
import me.itzisonn_.itzcosmetics.menu.InventoryData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Filter {
    private final ItzCosmetics plugin;
    private final InventoryData data;
    @Getter
    private ArrayList<Type> types;
    @Getter
    private ArrayList<Rarity> rarities;
    @Getter
    private ArrayList<Category> categories;
    @Getter
    private SortType sortType;
    @Getter
    private ShowType showType;
    @Getter @Setter
    private boolean showWithoutCategory;

    public Filter(ItzCosmetics plugin, InventoryData data) {
        this.plugin = plugin;
        this.data = data;

        types = new ArrayList<>(plugin.getShopManager().getTypes());
        rarities = new ArrayList<>(plugin.getShopManager().getRarities());
        categories = new ArrayList<>(plugin.getShopManager().getCategories());
        sortType = SortType.TYPE;
        showType = ShowType.ALL;
        showWithoutCategory = true;
    }

    public void updateData() {
        types = new ArrayList<>(plugin.getShopManager().getTypes());
        rarities = new ArrayList<>(plugin.getShopManager().getRarities());
        categories = new ArrayList<>(plugin.getShopManager().getCategories());
    }

    public ArrayList<Cosmetics> getFilteredCosmetics() {
        ArrayList<Cosmetics> cosmeticsList = new ArrayList<>(plugin.getShopManager().getCosmeticsList());

        cosmeticsList.removeIf(cosmetics -> !types.contains(cosmetics.getType()));

        cosmeticsList.removeIf(cosmetics -> !rarities.contains(cosmetics.getRarity()));

        cosmeticsList.removeIf(cosmetics -> {
            if (showWithoutCategory && cosmetics.getCategories().isEmpty()) return false;
            return Collections.disjoint(categories, cosmetics.getCategories());
        });

        if (sortType != SortType.TYPE) {
            cosmeticsList.sort(Comparator.comparingInt(o -> o.getRarity().getCost()));
            if (sortType == SortType.COST_INREVERSE) Collections.reverse(cosmeticsList);
        }

        switch (showType) {
            case ONLY_BOUGHT -> cosmeticsList.removeIf(cosmetics -> !plugin.getShopManager().isBought(plugin.getMenuManager().getPlayer(data), cosmetics));
            case ONLY_NOT_BOUGHT -> cosmeticsList.removeIf(cosmetics -> plugin.getShopManager().isBought(plugin.getMenuManager().getPlayer(data), cosmetics));
            case ONLY_EQUIPPED -> cosmeticsList.removeIf(cosmetics -> plugin.getShopManager().getUsed(plugin.getMenuManager().getPlayer(data), cosmetics.getType().getId()) != cosmetics);
            case ONLY_SALE -> cosmeticsList.removeIf(cosmetics -> plugin.getShopManager().getSale(cosmetics) == null);
        }

        return cosmeticsList;
    }

    public void nextShowType() {
        if (List.of(ShowType.values()).indexOf(showType) < List.of(ShowType.values()).size() - 1)
            showType = List.of(ShowType.values()).get(List.of(ShowType.values()).indexOf(showType) + 1);
    }

    public void prevShowType() {
        if (List.of(ShowType.values()).indexOf(showType) > 0) showType = List.of(ShowType.values()).get(List.of(ShowType.values()).indexOf(showType) - 1);
    }

    public void nextSortType() {
        if (List.of(SortType.values()).indexOf(sortType) < List.of(SortType.values()).size() - 1)
            sortType = List.of(SortType.values()).get(List.of(SortType.values()).indexOf(sortType) + 1);
    }

    public void prevSortType() {
        if (List.of(SortType.values()).indexOf(sortType) > 0) sortType = List.of(SortType.values()).get(List.of(SortType.values()).indexOf(sortType) - 1);
    }
}