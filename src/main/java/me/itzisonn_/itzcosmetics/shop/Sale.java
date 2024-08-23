package me.itzisonn_.itzcosmetics.shop;

import lombok.Getter;
import me.itzisonn_.itzcosmetics.ItzCosmetics;
import me.itzisonn_.itzcosmetics.cosmetics.Category;
import me.itzisonn_.itzcosmetics.cosmetics.Cosmetics;
import me.itzisonn_.itzcosmetics.cosmetics.Rarity;
import me.itzisonn_.itzcosmetics.cosmetics.type.Type;

import java.time.LocalDateTime;

@Getter
public class Sale {
    private final String id;
    private final LocalDateTime expireDate;
    private final int percent;
    private final Type cosmeticsType;
    private final Rarity cosmeticsRarity;
    private final Category cosmeticsCategory;
    private final String cosmeticsId;

    private Sale(String id, LocalDateTime expireDate, int percent, Type cosmeticsType, Rarity cosmeticsRarity, Category cosmeticsCategory, String cosmeticsId) {
        this.id = id;
        this.expireDate = expireDate;
        this.percent = percent;
        this.cosmeticsType = cosmeticsType;
        this.cosmeticsRarity = cosmeticsRarity;
        this.cosmeticsCategory = cosmeticsCategory;
        this.cosmeticsId = cosmeticsId;
    }

    @Override
    public String toString() {
        String typeId, rarityId, categoryId;
        if (cosmeticsType == null) typeId = "*";
        else typeId = cosmeticsType.getId();
        if (cosmeticsRarity == null) rarityId = "*";
        else rarityId = cosmeticsRarity.getId();
        if (cosmeticsCategory == null) categoryId = "*";
        else categoryId = cosmeticsCategory.getId();

        return "%s;%d.%d.%d;%d:%d:%d;%d;%s;%s;%s;%s".formatted(
                id,
                expireDate.getYear(), expireDate.getMonthValue(), expireDate.getDayOfMonth(),
                expireDate.getHour(), expireDate.getMinute(), expireDate.getSecond(),
                percent,
                typeId, rarityId, categoryId, cosmeticsId);
    }

    public static Sale fromString(String string, ItzCosmetics plugin) {
        String[] args = string.split(";");

        LocalDateTime expireDate;

        try {
            String[] date = args[1].split("\\.");
            String[] time = args[2].split(":");
            int year = Integer.parseInt(date[0]);
            int month = Integer.parseInt(date[1]);
            int day = Integer.parseInt(date[2]);
            int hour = Integer.parseInt(time[0]);
            int minute = Integer.parseInt(time[1]);
            int second = Integer.parseInt(time[2]);
            expireDate = LocalDateTime.of(year, month, day, hour, minute, second);
        }
        catch (NumberFormatException ignore) {
            return null;
        }

        return fromString(expireDate, "%s;%s;%s;%s;%s;%s".formatted(args[0], args[3], args[4], args[5], args[6], args[7]), plugin);
    }

    public static Sale fromString(LocalDateTime expireDate, String string, ItzCosmetics plugin) {
        String[] args = string.split(";");

        try {
            Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ignore) {
            return null;
        }

        if (!args[2].equals("*") && plugin.getShopManager().getType(args[2]) == null) return null;
        if (!args[3].equals("*") && plugin.getShopManager().getRarity(args[3]) == null) return null;
        if (!args[4].equals("*") && plugin.getShopManager().getCategory(args[4]) == null) return null;
        if (!args[5].equals("*") && plugin.getShopManager().getCosmetics(args[2], args[5]) == null) return null;

        return new Sale(
                args[0],
                expireDate, Integer.parseInt(args[1]),
                plugin.getShopManager().getType(args[2]), plugin.getShopManager().getRarity(args[3]), plugin.getShopManager().getCategory(args[4]), args[5]);
    }

    public boolean hasSale(Cosmetics cosmetics) {
        boolean hasSale;

        if (cosmeticsType != null) {
            hasSale = cosmetics.getType() == cosmeticsType;
            if (!hasSale) return false;
        }
        if (cosmeticsRarity != null) {
            hasSale = cosmetics.getRarity() == cosmeticsRarity;
            if (!hasSale) return false;
        }
        if (cosmeticsCategory != null) {
            hasSale = cosmetics.getCategories().contains(cosmeticsCategory);
            if (!hasSale) return false;
        }
        if (!cosmeticsId.equals("*")) {
            hasSale = cosmetics.getId().equalsIgnoreCase(cosmeticsId);
            return hasSale;
        }

        return true;
    }

    public int calculateCost(Cosmetics cosmetics) {
        try {
            return cosmetics.getRarity().getCost() * (100 - percent) / 100;
        }
        catch (NumberFormatException ignore) {
            return cosmetics.getRarity().getCost();
        }
    }

    public String getPercent() {
        return String.valueOf(percent);
    }
}