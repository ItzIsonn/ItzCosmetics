package me.itzisonn_.itzcosmetics;

import me.clip.placeholderapi.PlaceholderAPI;
import me.itzisonn_.itzcosmetics.cosmetics.Cosmetics;
import me.itzisonn_.itzcosmetics.cosmetics.Category;
import me.itzisonn_.itzcosmetics.menu.InventoryData;
import me.itzisonn_.itzcosmetics.shop.Sale;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private final ItzCosmetics plugin;

    public Utils(ItzCosmetics plugin) {
        this.plugin = plugin;
    }

    public String parsePlaceholders(Player player, String string) {
        return plugin.isHookedPapi() ?
                PlaceholderAPI.setPlaceholders(player, string) :
                string;
    }

    public String parseCosmeticsPlaceholders(String string, Player player, Cosmetics cosmetics) {
        ArrayList<String> categoriesList = new ArrayList<>();
        for (Category category : cosmetics.getCategories()) {
            if (categoriesList.size() >= plugin.getConfigManager().getCategoriesHideAmount()) {
                categoriesList.add(plugin.getConfigManager().getCategoriesHideReplace());
                break;
            }
            categoriesList.add(category.getName() + "<reset><white><i:false>");
        }
        if (categoriesList.isEmpty()) categoriesList.add("<white><i:false>" + plugin.getConfigManager().getCategoriesEmpty());

        String categories = categoriesList.toString().replaceAll("[\\[\\]]", "").replace(", ", plugin.getConfigManager().getCategoriesSeparator());

        String comment = plugin.getConfigManager().getComment(getStatusId(player, cosmetics));
        string = string
                .replace("%name%", cosmetics.getName())
                .replace("%name_raw%", MiniMessage.miniMessage().stripTags(cosmetics.getName()))
                .replace("%type%", cosmetics.getType().getName())
                .replace("%type_raw%", MiniMessage.miniMessage().stripTags(cosmetics.getType().getName()))
                .replace("%rarity%", cosmetics.getRarity().getName())
                .replace("%rarity_raw%", MiniMessage.miniMessage().stripTags(cosmetics.getRarity().getName()))
                .replace("%categories%", categories)
                .replace("%categories_raw%", MiniMessage.miniMessage().stripTags(categories))
                .replace("%cost%", String.valueOf(cosmetics.getRarity().getCost()))
                .replace("%comment%", comment)
                .replace("%comment_raw%", MiniMessage.miniMessage().stripTags(comment));


        Sale sale = plugin.getShopManager().getSale(cosmetics);

        if (sale != null) {
            string = string
                    .replace("%final_cost%", String.valueOf(sale.calculateCost(cosmetics)))
                    .replace("%sale_cost%", String.valueOf(sale.calculateCost(cosmetics)))
                    .replace("%sale_percent%", sale.getPercent());

            String toReplace;

            if (LocalDateTime.now().until(sale.getExpireDate(), ChronoUnit.YEARS) > 0) {
                toReplace = parseDate(plugin.getConfigManager().getExpiryTime("overYear"), sale.getExpireDate());
            }
            else if (LocalDateTime.now().until(sale.getExpireDate(), ChronoUnit.MONTHS) > 0) {
                toReplace = parseDate(plugin.getConfigManager().getExpiryTime("overMonth"), sale.getExpireDate());
            }
            else if (LocalDateTime.now().until(sale.getExpireDate(), ChronoUnit.DAYS) > 0) {
                toReplace = parseDate(plugin.getConfigManager().getExpiryTime("overDay"), sale.getExpireDate());
            }
            else if (LocalDateTime.now().until(sale.getExpireDate(), ChronoUnit.HOURS) > 0) {
                toReplace = parseDate(plugin.getConfigManager().getExpiryTime("overHour"), sale.getExpireDate());
            }
            else if (LocalDateTime.now().until(sale.getExpireDate(), ChronoUnit.MINUTES) > 0) {
                toReplace = parseDate(plugin.getConfigManager().getExpiryTime("overMinute"), sale.getExpireDate());
            }
            else {
                toReplace = parseDate(plugin.getConfigManager().getExpiryTime("other"), sale.getExpireDate());
            }

            string = string.replace("%expiry_time%", toReplace);
        }
        else {
            string = string.replace("%final_cost%", String.valueOf(cosmetics.getRarity().getCost()));
        }

        return string;
    }

    private String parseDate(String string, LocalDateTime time) {
        return string
                .replace("%years%", String.valueOf(Period.between(LocalDateTime.now().toLocalDate(), time.toLocalDate()).getYears()))
                .replace("%months%", String.valueOf(Period.between(LocalDateTime.now().toLocalDate(), time.toLocalDate()).getMonths()))
                .replace("%days%", String.valueOf(Period.between(LocalDateTime.now().toLocalDate(), time.toLocalDate()).getDays()))
                .replace("%hours%", String.valueOf(Duration.between(LocalDateTime.now(), time).toHoursPart()))
                .replace("%minutes%", String.valueOf(Duration.between(LocalDateTime.now(), time).toMinutesPart()))
                .replace("%seconds%", String.valueOf(Duration.between(LocalDateTime.now(), time).toSecondsPart()));
    }

    public String parseEntitiesPlaceholders(String string, HashMap<String, Entity> entities) {
        if (entities.size() == 1) {
            Entity entity = new ArrayList<>(entities.values()).get(0);

            string = string
                    .replace("[name]", entity.getName())
                    .replace("[world]", entity.getWorld().getName())
                    .replace("[x]", String.valueOf(entity.getX()))
                    .replace("[y]", String.valueOf(entity.getY()))
                    .replace("[z]", String.valueOf(entity.getZ()));
        }

        for (String placeholder : entities.keySet()) {
            Entity entity = entities.get(placeholder);

            string = string
                    .replace("[" + placeholder + "_name]", entity.getName())
                    .replace("[" + placeholder + "_world]", entity.getWorld().getName())
                    .replace("[" + placeholder + "_x]", String.valueOf(entity.getX()))
                    .replace("[" + placeholder + "_y]", String.valueOf(entity.getY()))
                    .replace("[" + placeholder + "_z]", String.valueOf(entity.getZ()));
        }

        return string;
    }

    public String parseMenuPlaceholders(InventoryData data, String string) {
        string = string
                .replace("%page_num%", String.valueOf(data.getPage() + 1))
                .replace("%page_total%", String.valueOf(plugin.getConfigManager().getPages(data).size()))

                .replace("%filter_showtype%", plugin.getConfigManager().getShowTypeTranslation(data.getFilter().getShowType()))
                .replace("%filter_sorttype%", plugin.getConfigManager().getSortTypeTranslation(data.getFilter().getSortType()));

        if (plugin.getConfigManager().getPages(data).size() > data.getPage()) {
            string = string.replace("%page_name%", plugin.getConfigManager().getPageName(data));
        }

        Matcher typeMatcher = Pattern.compile("%filter_type_([a-zA-Z_]+)%").matcher(string);
        while (typeMatcher.find()) {
            String type = typeMatcher.group(1);
            string = string.replace("%filter_type_" + type + "%",
                    plugin.getConfigManager().getStatusTranslation(data.getFilter().getTypes().contains(plugin.getShopManager().getType(type))));
        }

        Matcher rarityMatcher = Pattern.compile("%filter_rarity_([a-zA-Z_]+)%").matcher(string);
        while (rarityMatcher.find()) {
            String rarity = rarityMatcher.group(1);
            string = string.replace("%filter_rarity_" + rarity + "%",
                    plugin.getConfigManager().getStatusTranslation(data.getFilter().getRarities().contains(plugin.getShopManager().getRarity(rarity))));
        }

        Matcher categoryMatcher = Pattern.compile("%filter_category_([a-zA-Z_]+)%").matcher(string);
        while (categoryMatcher.find()) {
            String category = categoryMatcher.group(1);
            if (category.equals("without")) {
                string = string.replace("%filter_category_without%",
                        plugin.getConfigManager().getStatusTranslation(data.getFilter().isShowWithoutCategory()));
            }
            else string = string.replace("%filter_category_" + category + "%",
                    plugin.getConfigManager().getStatusTranslation(data.getFilter().getCategories().contains(plugin.getShopManager().getCategory(category))));
        }

        return string;
    }

    public String getStatusId(Player player, Cosmetics cosmetics) {
        String statusId;

        if (plugin.getShopManager().isBought(player, cosmetics)) {
            if (plugin.getShopManager().getUsed(player, cosmetics.getType().getId()) == cosmetics) statusId = "alreadyInUse";
            else statusId = "use";
        }
        else {
            int cost = cosmetics.getRarity().getCost();
            if (plugin.getShopManager().getSale(cosmetics) != null) cost = plugin.getShopManager().getSale(cosmetics).calculateCost(cosmetics);

            if (plugin.getShopManager().getMoney(player) >= cost) statusId = "buy";
            else statusId = "notEnoughMoney";
        }

        return statusId;
    }

    public void doAction(String target, String action) {
        action = calculate(action);
        String actionId = action.split(":")[0].toUpperCase();
        String[] args = action.replaceAll("^" + actionId + ":", "").split(";");

        Location location = null;

        if (actionId.equalsIgnoreCase("PARTICLE") || actionId.equalsIgnoreCase("SOUND")) {
            World world = Bukkit.getWorld(args[0]);
            if (world == null) return;

            try {
                location = new Location(world, Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]));
            }
            catch (NumberFormatException ignore) {
                return;
            }
        }

        ArrayList<Player> players = new ArrayList<>();
        if (target.equalsIgnoreCase("ALL")) {
            players.addAll(Bukkit.getOnlinePlayers());
        }
        else if (target.matches("R:.*") && location != null) {
            float distance;

            try {
                distance = Float.parseFloat(target.replaceAll("^R:", ""));
            }
            catch (NumberFormatException ignore) {
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getLocation().distanceSquared(location) <= Math.pow(distance, 2)) players.add(player);
            }
        }
        else  {
            Player player = Bukkit.getPlayer(target);
            if (player == null) return;
            players.add(player);
        }

        for (Player player : players) {
            switch (actionId) {
                case "PARTICLE" -> {
                    Particle particle;
                    int count;
                    double offsetX, offsetY, offsetZ, speed;

                    try {
                        particle = Particle.valueOf(args[4]);
                        count = Integer.parseInt(args[5]);
                        offsetX = Double.parseDouble(args[6]);
                        offsetY = Double.parseDouble(args[7]);
                        offsetZ = Double.parseDouble(args[8]);
                        speed = Double.parseDouble(args[9]);
                    }
                    catch (IllegalArgumentException ignore) {
                        return;
                    }

                    if (location != null) player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
                }

                case "SOUND" -> {
                    Sound sound;
                    float volume, pitch;

                    try {
                        sound = Sound.valueOf(args[4]);
                        volume = Float.parseFloat(args[5]);
                        pitch = Float.parseFloat(args[6]);
                    }
                    catch (IllegalArgumentException ignore) {
                        continue;
                    }

                    if (location != null) player.playSound(location, sound, volume, pitch);
                }

                case "MESSAGE" -> player.sendMessage(MiniMessage.miniMessage().deserialize(action.replaceAll("^" + actionId + ":", "")));

                case "PLAYER" -> player.chat("/" + action.replaceAll("^" + actionId + ":", ""));

                case "CONSOLE" -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), action.replaceAll("^" + actionId + ":", ""));

                case "WAIT" -> {
                    try {
                        Thread.sleep(Long.parseLong(action.replaceAll("^" + actionId + ":", "")) * 50);
                    }
                    catch (InterruptedException ignored) {}
                }
            }
        }
    }

    private String calculate(String string) {
        for (String operator : List.of("\\+", "-", "\\*", "/")) {
            Matcher matcher = Pattern.compile("((-?)(0|([1-9][0-9]*))(\\.[0-9]+)?)\\s*" + operator + "\\s*((-?)(0|([1-9][0-9]*))(\\.[0-9]+)?)").matcher(string);

            while (matcher.find()) {
                try {
                    double a = Double.parseDouble(matcher.group(1));
                    double b = Double.parseDouble(matcher.group(6));

                    String value = switch (operator) {
                        case "\\+" -> String.valueOf(a + b);
                        case "-" -> String.valueOf(a - b);
                        case "\\*" -> String.valueOf(a * b);
                        case "/" -> String.valueOf(a / b);
                        default -> "0";
                    };

                    if (value.endsWith(".0")) value = value.replaceAll(".0$", "");

                    string = string.replaceAll(matcher.group(1) + "\\s*" + operator + "\\s*" + matcher.group(6), value);
                }
                catch (NumberFormatException ignore) {}
            }
        }

        return string;
    }
}