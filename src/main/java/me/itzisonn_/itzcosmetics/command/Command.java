package me.itzisonn_.itzcosmetics.command;

import me.itzisonn_.itzcosmetics.ItzCosmetics;
import me.itzisonn_.itzcosmetics.cosmetics.Cosmetics;
import me.itzisonn_.itzcosmetics.menu.MenuType;
import me.itzisonn_.itzcosmetics.shop.Sale;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Command extends AbstractCommand {
    private final ItzCosmetics plugin;

    public Command(ItzCosmetics plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("itzcosmetics.cosmetics") && !sender.hasPermission("itzcosmetics.reload") && !sender.hasPermission("itzcosmetics.edit")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("noPermission")));
            return;
        }

        if (args.length == 0) {
            if (!sender.hasPermission("itzcosmetics.cosmetics")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownCommand")));
                return;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("onlyPlayer")));
                return;
            }

            plugin.getMenuManager().open(((Player) sender), MenuType.COSMETICS);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("itzcosmetics.reload")) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownCommand")));
                    return;
                }

                if (args.length > 1) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("tooManyArguments")));
                    return;
                }

                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("reloaded")));
                plugin.reload();
            }

            case "bought" -> {
                if (!sender.hasPermission("itzcosmetics.edit")) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownCommand")));
                    return;
                }

                if (args.length > 5) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("tooManyArguments")));
                    return;
                }

                if (args.length < 5) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("notEnoughArguments")));
                    return;
                }

                Cosmetics cosmetics = plugin.getShopManager().getCosmetics(args[3], args[4]);
                ArrayList<Player> players = new ArrayList<>();

                if (args[2].equals("*")) {
                    players = new ArrayList<>(Bukkit.getOnlinePlayers());
                }
                else {
                    if (Bukkit.getPlayer(args[2]) == null) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownPlayer")
                                .replace("%player%", args[2])));
                        return;
                    }
                    players.add(Bukkit.getPlayer(args[2]));
                }

                if (cosmetics == null) {
                    if (plugin.getShopManager().getType(args[3]) == null) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownType")
                                .replace("%type%", args[3])));
                    }
                    else {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownType")
                                .replace("%type%", args[3]).replace("%id%", args[4])));
                    }
                    return;
                }

                switch (args[1].toLowerCase()) {
                    case "set" -> {
                        ArrayList<String> success = new ArrayList<>();

                        for (Player player : players) {
                            if (plugin.getShopManager().isBought(player, cosmetics)) continue;

                            plugin.getShopManager().setBought(player, cosmetics, true);
                            success.add(player.getName());
                        }

                        if (!success.isEmpty()) sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("success")
                                .replace("%players%", success.toString().replaceAll("[\\[\\]]", ""))));
                        else sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("notSuccess")));
                    }

                    case "unset" -> {
                        ArrayList<String> success = new ArrayList<>();

                        for (Player player : players) {
                            if (!plugin.getShopManager().isBought(player, cosmetics)) continue;

                            plugin.getShopManager().setBought(player, cosmetics, false);
                            success.add(player.getName());
                        }

                        if (!success.isEmpty()) sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("success")
                                .replace("%players%", success.toString().replaceAll("[\\[\\]]", ""))));
                        else sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("notSuccess")));
                    }
                }
            }

            case "setused" -> {
                if (!sender.hasPermission("itzcosmetics.edit")) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownCommand")));
                    return;
                }

                if (args.length > 4) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("tooManyArguments")));
                    return;
                }

                if (args.length < 4) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("notEnoughArguments")));
                    return;
                }

                Cosmetics cosmetics = plugin.getShopManager().getCosmetics(args[2], args[3]);
                ArrayList<Player> players = new ArrayList<>();

                if (args[1].equals("*")) {
                    players = new ArrayList<>(Bukkit.getOnlinePlayers());
                }
                else {
                    if (Bukkit.getPlayer(args[1]) == null) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownPlayer")
                                .replace("%player%", args[1])));
                        return;
                    }
                    players.add(Bukkit.getPlayer(args[1]));
                }

                if (cosmetics == null) {
                    if (plugin.getShopManager().getType(args[2]) == null) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownType")
                                .replace("%type%", args[2])));
                    }
                    else {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownType")
                                .replace("%type%", args[2]).replace("%id%", args[3])));
                    }
                    return;
                }

                ArrayList<String> success = new ArrayList<>();

                for (Player player : players) {
                    if (plugin.getShopManager().getUsed(player, args[2]) == cosmetics) continue;

                    if (!plugin.getShopManager().isBought(player, cosmetics)) plugin.getShopManager().setBought(player, cosmetics, true);
                    plugin.getShopManager().setUsed(player, cosmetics);
                    success.add(player.getName());
                }

                if (!success.isEmpty()) sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("success")
                        .replace("%players%", success.toString().replaceAll("[\\[\\]]", ""))));
                else sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("notSuccess")));
            }

            case "sale" -> {
                if (!sender.hasPermission("itzcosmetics.edit")) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownCommand")));
                    return;
                }

                switch (args[1].toLowerCase()) {
                    case "add" -> {
                        if (args.length > 10) {
                            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("tooManyArguments")));
                            return;
                        }

                        if (args.length < 10) {
                            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("notEnoughArguments")));
                            return;
                        }

                        LocalDateTime expireDate;

                        try {
                            String[] date = args[4].split(";")[0].split("\\.");
                            String[] time = args[4].split(";")[1].split(":");
                            int year = Integer.parseInt(date[0]);
                            int month = Integer.parseInt(date[1]);
                            int day = Integer.parseInt(date[2]);
                            int hour = Integer.parseInt(time[0]);
                            int minute = Integer.parseInt(time[1]);
                            int second = Integer.parseInt(time[2]);

                            switch (args[3].toLowerCase()) {
                                case "exact" -> {
                                    try {
                                        expireDate = LocalDateTime.of(year, month, day, hour, minute, second);
                                        if (expireDate.isBefore(LocalDateTime.now())) {
                                            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("invalidDate")));
                                            return;
                                        }
                                    }
                                    catch (DateTimeException ignore) {
                                        sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("invalidDate")));
                                        return;
                                    }
                                }
                                case "after" -> expireDate = LocalDateTime.now()
                                        .plusYears(year).plusMonths(month).plusDays(day)
                                        .plusHours(hour).plusMinutes(minute).plusSeconds(second);
                                default -> {
                                    sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownArgument")
                                            .replace("%args%", "exact | after")));
                                    return;
                                }
                            }
                        }
                        catch (NumberFormatException ignore) {
                            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("invalidDate")));
                            return;
                        }

                        if (plugin.getShopManager().getSale(args[2]) != null) {
                            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("saleAlreadyExists")));
                            return;
                        }

                        Sale sale = Sale.fromString(expireDate, "%s;%s;%s;%s;%s;%s".formatted(args[2], args[5], args[6], args[7], args[8], args[9]), plugin);
                        if (sale == null) {
                            try {
                                Integer.parseInt(args[5]);
                            }
                            catch (NumberFormatException ignore) {
                                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("percentOnlyNumbers")));
                                return;
                            }

                            if (!args[6].equals("*") && plugin.getShopManager().getType(args[6]) == null) {
                                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownType")
                                        .replace("%type%", args[6])));
                                return;
                            }
                            if (!args[7].equals("*") && plugin.getShopManager().getRarity(args[7]) == null) {
                                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownRarity")
                                        .replace("%rarity%", args[7])));
                                return;
                            }
                            if (!args[8].equals("*") && plugin.getShopManager().getCategory(args[8]) == null) {
                                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownCategory")
                                        .replace("%category%", args[8])));
                                return;
                            }
                            if (!args[9].equals("*") && plugin.getShopManager().getCosmetics(args[6], args[9]) == null) {
                                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("unknownCosmetics")
                                        .replace("%type%", args[6]).replace("%id%", args[9])));
                                return;
                            }

                            sender.sendMessage(MiniMessage.miniMessage().deserialize("Unknown error occurred, report this to plugin's author"));
                            return;
                        }

                        plugin.getShopManager().addSale(sale);
                        sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("successSale")));
                    }

                    case "remove" -> {
                        if (args.length > 3) {
                            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("tooManyArguments")));
                            return;
                        }

                        if (args.length < 3) {
                            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("notEnoughArguments")));
                            return;
                        }

                        Sale sale = plugin.getShopManager().getSale(args[2]);
                        if (sale == null) {
                            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("saleDoesNotExist")));
                            return;
                        }

                        plugin.getShopManager().removeSale(args[2]);
                        sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("successSale")));
                    }

                    case "list" -> {
                        if (args.length > 2) {
                            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("tooManyArguments")));
                            return;
                        }

                        for (int i = 0; i < plugin.getShopManager().getSalesCache().size(); i++) {
                            Sale sale = Sale.fromString(plugin.getShopManager().getSalesCache().get(i), plugin);
                            if (sale == null) continue;
                            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfigManager().getMessage("saleTemplate")
                                    .replace("%pos%", String.valueOf(i + 1))
                                    .replace("%id%", sale.getId())
                                    .replace("%expire_date%", sale.getExpireDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd;hh:mm:ss")))
                                    .replace("%percent%", String.valueOf(sale.getPercent()))
                                    .replace("%cosmetics_type%", sale.getCosmeticsType() == null ? "*" : sale.getCosmeticsType().getName())
                                    .replace("%cosmetics_type_raw%", sale.getCosmeticsType() == null ? "*" : MiniMessage.miniMessage().stripTags(sale.getCosmeticsType().getName()))
                                    .replace("%cosmetics_rarity%", sale.getCosmeticsRarity() == null ? "*" : sale.getCosmeticsRarity().getName())
                                    .replace("%cosmetics_rarity_raw%", sale.getCosmeticsRarity() == null ? "*" : MiniMessage.miniMessage().stripTags(sale.getCosmeticsRarity().getName()))
                                    .replace("%cosmetics_category%", sale.getCosmeticsCategory() == null ? "*" : sale.getCosmeticsCategory().getName())
                                    .replace("%cosmetics_category_raw%", sale.getCosmeticsCategory() == null ? "*" : MiniMessage.miniMessage().stripTags(sale.getCosmeticsCategory().getName()))
                                    .replace("%cosmetics_id%", sale.getCosmeticsId() == null ? "*" : sale.getCosmeticsId())));
                        }
                    }
                }
            }
        }
    }
}