package nzst.laminan.falconforce;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public class PlayerListener implements Listener {
    private final FalconForce plugin;

    public PlayerListener(FalconForce plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("falcon.force")) {
            plugin.getLoggedIn().put(player.getName(), false);
            player.sendMessage(ChatColor.RED + "You must log in using /facon.");
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Map<String, Boolean> loggedIn = plugin.getLoggedIn();
        
        if (player.hasPermission("falcon.force") && !loggedIn.getOrDefault(player.getName(), false)) {
            String command = event.getMessage().split(" ")[0].toLowerCase();
            if (!command.equals("/login") && !command.equals("/facon")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You are restricted to /login and /facon until verified.");
            }
        }
    }
}

