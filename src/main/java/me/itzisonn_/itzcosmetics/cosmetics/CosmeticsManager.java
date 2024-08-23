package me.itzisonn_.itzcosmetics.cosmetics;

import me.itzisonn_.itzcosmetics.ItzCosmetics;
import me.itzisonn_.itzcosmetics.cosmetics.type.ActivatorType;
import me.itzisonn_.itzcosmetics.cosmetics.type.Type;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.*;

public class CosmeticsManager implements Listener {
    private final ItzCosmetics plugin;
    private final HashMap<Projectile, Player> projectiles = new HashMap<>();
    private final HashMap<Cosmetics, Integer> animatedPlaceholders = new HashMap<>();

    public CosmeticsManager(ItzCosmetics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.getShopManager().updateCosmetics(e.getPlayer());

        for (Cosmetics cosmetics : plugin.getShopManager().getCosmeticsByActivatorType(ActivatorType.JOIN)) {
            if (!plugin.getShopManager().getUsed(e.getPlayer(), cosmetics.getType().getId()).equals(cosmetics)) continue;

            HashMap<String, Entity> entities = new HashMap<>();
            entities.put("player", e.getPlayer());

            parseActions(cosmetics, entities);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        for (Cosmetics cosmetics : plugin.getShopManager().getCosmeticsByActivatorType(ActivatorType.QUIT)) {
            if (!plugin.getShopManager().getUsed(e.getPlayer(), cosmetics.getType().getId()).equals(cosmetics)) continue;

            HashMap<String, Entity> entities = new HashMap<>();
            entities.put("player", e.getPlayer());

            parseActions(cosmetics, entities);
        }
    }

    @EventHandler
    public void onKill(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;

        for (Cosmetics cosmetics : plugin.getShopManager().getCosmeticsByActivatorType(ActivatorType.KILL)) {
            if (!plugin.getShopManager().getUsed(e.getEntity().getKiller(), cosmetics.getType().getId()).equals(cosmetics)) continue;
            if (cosmetics.getType().getActivatorArgs()[0].equalsIgnoreCase("PLAYER") && e.getEntity().getType() != EntityType.PLAYER) continue;
            if (cosmetics.getType().getActivatorArgs()[0].equalsIgnoreCase("ENTITY") && e.getEntity().getType() == EntityType.PLAYER) continue;

            HashMap<String, Entity> entities = new HashMap<>();
            entities.put("opponent", e.getEntity());
            entities.put("player", e.getEntity().getKiller());

            parseActions(cosmetics, entities);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        for (Cosmetics cosmetics : plugin.getShopManager().getCosmeticsByActivatorType(ActivatorType.DEATH)) {
            if (!plugin.getShopManager().getUsed(e.getPlayer(), cosmetics.getType().getId()).equals(cosmetics)) continue;

            HashMap<String, Entity> entities = new HashMap<>();
            entities.put("player", e.getPlayer());
            if (e.getPlayer().getKiller() != null) entities.put("killer", e.getPlayer().getKiller());

            parseActions(cosmetics, entities);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        for (Cosmetics cosmetics : plugin.getShopManager().getCosmeticsByActivatorType(ActivatorType.RESPAWN)) {
            if (!plugin.getShopManager().getUsed(e.getPlayer(), cosmetics.getType().getId()).equals(cosmetics)) continue;

            HashMap<String, Entity> entities = new HashMap<>();
            entities.put("player", e.getPlayer());

            parseActions(cosmetics, entities);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!e.hasChangedPosition()) return;

        for (Cosmetics cosmetics : plugin.getShopManager().getCosmeticsByActivatorType(ActivatorType.MOVE)) {
            if (!cosmetics.getType().getActivatorArgs()[0].equalsIgnoreCase("PLAYER")) continue;
            if (!plugin.getShopManager().getUsed(e.getPlayer(), cosmetics.getType().getId()).equals(cosmetics)) continue;

            HashMap<String, Entity> entities = new HashMap<>();
            entities.put("player", e.getPlayer());

            parseActions(cosmetics, entities);
        }
    }

    public void onProjectileMove() {
        Iterator<Projectile> iterator = projectiles.keySet().iterator();

        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();

            if (projectile.isOnGround() || projectile.isDead()) {
                iterator.remove();
                continue;
            }

            Player player = projectiles.get(projectile);

            if (!player.isOnline()) {
                iterator.remove();
                continue;
            }

            for (Cosmetics cosmetics : plugin.getShopManager().getCosmeticsByActivatorType(ActivatorType.MOVE)) {
                if (!cosmetics.getType().getActivatorArgs()[0].equalsIgnoreCase("PROJECTILE")) continue;
                if (!plugin.getShopManager().getUsed(player, cosmetics.getType().getId()).equals(cosmetics)) continue;

                HashMap<String, Entity> entities = new HashMap<>();
                entities.put("projectile", projectile);
                entities.put("player", player);

                parseActions(cosmetics, entities);
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity().getOwnerUniqueId() == null) return;
        Player player = Bukkit.getPlayer(e.getEntity().getOwnerUniqueId());
        if (player == null) return;
        projectiles.putIfAbsent(e.getEntity(), player);
    }

    public void startRepeat() {
        for (Type type : plugin.getShopManager().getTypes()) {
            if (type.getActivatorType() != ActivatorType.REPEAT) continue;

            plugin.getTaskIds().add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    Cosmetics cosmetics = plugin.getShopManager().getUsed(player, type.getId());

                    HashMap<String, Entity> entities = new HashMap<>();
                    entities.put("player", player);

                    parseActions(cosmetics, entities);
                }
            }, 0, Integer.parseInt(type.getActivatorArgs()[0])));
        }
    }

    public void startPlaceholderUpdate() {
        for (Type type : plugin.getShopManager().getTypes()) {
            if (type.getActivatorType() != ActivatorType.ANIMATED_PLACEHOLDER) continue;

            plugin.getTaskIds().add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                for (Cosmetics cosmetics : plugin.getShopManager().getCosmeticsByType(type.getId())) {
                    animatedPlaceholders.putIfAbsent(cosmetics, 0);
                    if (cosmetics.getData().get("lines") == null) continue;

                    int line = animatedPlaceholders.get(cosmetics);
                    ArrayList<String> lines = toList(cosmetics.getData().get("lines"));

                    if (line + 1 >= lines.size()) animatedPlaceholders.put(cosmetics, 0);
                    else animatedPlaceholders.put(cosmetics, line + 1);
                }
            }, 0, Integer.parseInt(type.getActivatorArgs()[0])));
        }
    }

    public String getPlaceholder(Cosmetics cosmetics) {
        if (cosmetics.getData().get("lines") == null) return "";

        return toList(cosmetics.getData().get("lines")).get(animatedPlaceholders.get(cosmetics));
    }

    public void onUse(Player player, Cosmetics cosmetics) {
        HashMap<String, Entity> entities = new HashMap<>();
        entities.put("player", player);

        parseActions(cosmetics, entities);
    }



    private void parseActions(Cosmetics cosmetics, HashMap<String, Entity> entities) {
        HashMap<String, ArrayList<String>> actionsMap = new HashMap<>(cosmetics.getType().getActions());

        for (String actionGroup : cosmetics.getType().getActions().keySet()) {
            ArrayList<String> groupActions = cosmetics.getType().getActions().get(actionGroup);
            ArrayList<String> newGroupActions = new ArrayList<>();

            for (String action : groupActions) {
                for (String key : cosmetics.getData().keySet()) {
                    action = plugin.getUtils().parseEntitiesPlaceholders(action.replace("{" + key + "}", cosmetics.getData().get(key).toString()), entities);
                }

                newGroupActions.add(action);
            }

            actionsMap.put(actionGroup, newGroupActions);
        }


        if (actionsMap.get("default") == null) return;

        new Thread(() -> {
            for (String stringAction : actionsMap.get("default")) {
                String target = stringAction.split("\\*")[0];
                String action = stringAction.replaceAll("^" + target + "\\*", "");

                String actionId = action.split(":")[0].toUpperCase();
                String[] args = action.replaceAll("^" + actionId + ":", "").split(";");
                if (action.equals(stringAction)) {
                    if (actionId.equalsIgnoreCase("RANDOM")) {
                        SplittableRandom random = new SplittableRandom();
                        int chance = random.nextInt(1, 101);
                        int usedPercent = 0;

                        for (String group : args) {
                            int percent;
                            try {
                                percent = Integer.parseInt(group.split("%")[1]);
                            }
                            catch (NumberFormatException ignore) {
                                continue;
                            }
                            usedPercent += percent;

                            if (chance >= usedPercent - percent && chance < usedPercent) {
                                new Thread(() -> {
                                    for (String chanceStringAction : actionsMap.get(group.split("%")[0])) {
                                        String chanceTarget = chanceStringAction.split("\\*")[0];
                                        String chanceAction = chanceStringAction.replaceAll("^" + chanceTarget + "\\*", "");
                                        plugin.getUtils().doAction(chanceTarget, chanceAction);
                                    }
                                }).start();
                            }
                        }
                    }

                    continue;
                }

                plugin.getUtils().doAction(target, action);
            }
        }).start();
    }



    private ArrayList<String> toList(Object list) {
        ArrayList<String> strings = new ArrayList<>();

        if (list instanceof Collection<?> collection) {
            for (Object object : collection) {
                strings.add(object.toString());
            }
        }

        return strings;
    }
}