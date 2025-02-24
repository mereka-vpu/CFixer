package nzst.laminan.falconforce.commandblocker;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;
import java.util.UUID;

public class CommandBlocker implements Listener {
    private final FalconForce plugin;
    private final Set<UUID> verifiedPlayers;

    public CommandBlocker(FalconForce plugin, Set<UUID> verifiedPlayers) {
        this.plugin = plugin;
        this.verifiedPlayers = verifiedPlayers;
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String command = event.getMessage().toLowerCase();

        if (!verifiedPlayers.contains(uuid) && !command.startsWith("/login") && !command.startsWith("/facon")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must log in first using /facon!");
        }
    }
}
