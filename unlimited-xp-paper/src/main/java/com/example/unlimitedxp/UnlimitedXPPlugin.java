package com.example.unlimitedxp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UnlimitedXPPlugin extends JavaPlugin implements CommandExecutor {

    private final Set<UUID> activePlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<UUID, Integer> playerLevels = new ConcurrentHashMap<>();

    private static final int DEFAULT_LEVEL = 100;
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 1000;

    @Override
    public void onEnable() {
        if (this.getCommand("unlimitedxp") != null) {
            this.getCommand("unlimitedxp").setExecutor(this);
        }

        // Paper server tick task (runs every 1 second / 20 ticks)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (UUID uuid : activePlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline() && player.isValid()) {
                    int target = playerLevels.getOrDefault(uuid, DEFAULT_LEVEL);
                    if (player.getLevel() < target) {
                        player.setLevel(target);
                    }
                }
            }
        }, 20L, 20L);

        getLogger().info("UnlimitedXP Paper plugin enabled!");
    }

    @Override
    public void onDisable() {
        activePlayers.clear();
        playerLevels.clear();
        getLogger().info("UnlimitedXP Paper plugin disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by players on the Paper server.");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (args.length == 0 || args[0].equalsIgnoreCase("toggle")) {
            if (activePlayers.contains(uuid)) {
                activePlayers.remove(uuid);
                player.sendMessage(ChatColor.GOLD + "[Unlimited XP] " + ChatColor.RED + "Personal auto-XP disabled.");
            } else {
                activePlayers.add(uuid);
                int target = playerLevels.getOrDefault(uuid, DEFAULT_LEVEL);
                player.sendMessage(ChatColor.GOLD + "[Unlimited XP] " + ChatColor.GREEN + "Personal auto-XP enabled! (Level: " + target + ")");
                if (player.getLevel() < target) {
                    player.setLevel(target);
                }
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.YELLOW + "Usage: /unlimitedxp set <1-" + MAX_LEVEL + ">");
                return true;
            }

            try {
                int level = Integer.parseInt(args[1]);
                if (level < MIN_LEVEL || level > MAX_LEVEL) {
                    player.sendMessage(ChatColor.RED + "Level must be between " + MIN_LEVEL + " and " + MAX_LEVEL + ".");
                    return true;
                }
                playerLevels.put(uuid, level);
                activePlayers.add(uuid);
                player.setLevel(level);
                player.sendMessage(ChatColor.GOLD + "[Unlimited XP] " + ChatColor.GREEN + "Level set to " + level + "!");
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Please provide a valid integer.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("status")) {
            boolean active = activePlayers.contains(uuid);
            int target = playerLevels.getOrDefault(uuid, DEFAULT_LEVEL);
            player.sendMessage(ChatColor.GOLD + "[Unlimited XP] " +
                "Status: " + (active ? ChatColor.GREEN + "ACTIVE" : ChatColor.RED + "INACTIVE") +
                ChatColor.GRAY + " | Target: " + target);
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Usage: /unlimitedxp [toggle | set <level> | status]");
        return true;
    }
}
