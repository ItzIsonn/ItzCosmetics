package me.itzisonn_.itzcosmetics.shop;

import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.entity.Player;

public class PlayerPointsHook {
    public static int getMoney(Player player) {
        return PlayerPoints.getInstance().getAPI().look(player.getUniqueId());
    }

    public static void changeMoney(Player player, int change) {
        PlayerPoints.getInstance().getAPI().give(player.getUniqueId(), change);
    }
}