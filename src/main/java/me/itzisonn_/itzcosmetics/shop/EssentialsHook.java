package me.itzisonn_.itzcosmetics.shop;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import net.ess3.api.MaxMoneyException;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class EssentialsHook {
    private static final Essentials essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");

    public static int getMoney(Player player) {
        assert essentials != null;
        return Economy.getMoneyExact(essentials.getUser(player)).intValue();
    }

    public static void changeMoney(Player player, int change) {
        try {
            assert essentials != null;
            Economy.add(essentials.getUser(player), BigDecimal.valueOf(change));
        }
        catch (NoLoanPermittedException | MaxMoneyException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("<red>[ItzCosmetics] An error occurred when changing the player's balance (\" + player.getName() + \")"));
        }
    }
}