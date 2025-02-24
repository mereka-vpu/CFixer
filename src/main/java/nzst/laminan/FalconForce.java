package nzst.laminan.falconforce;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class FalconForce extends JavaPlugin implements CommandExecutor {
    private final Map<String, Boolean> loggedIn = new HashMap<>();
    private final Map<String, String> pendingEmails = new HashMap<>();

    @Override
    public void onEnable() {
        this.getCommand("login").setExecutor(this);
        this.getCommand("facon").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        
        if (command.getName().equalsIgnoreCase("login")) {
            if (loggedIn.getOrDefault(player.getName(), false)) {
                player.sendMessage(ChatColor.RED + "You are already logged in.");
                return true;
            }
            player.sendMessage(ChatColor.GREEN + "You need to use /facon to verify your login.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("facon")) {
            if (loggedIn.getOrDefault(player.getName(), false)) {
                player.sendMessage(ChatColor.RED + "You are already logged in.");
                return true;
            }
            
            String email = getEmailFromAPI(player.getName());
            if (email == null) {
                player.sendMessage(ChatColor.RED + "Could not find registered email.");
                return true;
            }
            
            pendingEmails.put(player.getName(), email);
            sendVerificationEmail(player, email);
            player.sendMessage(ChatColor.YELLOW + "We sent an email to " + maskEmail(email));
        }
        return true;
    }
    
    public void verifyPlayer(String username) {
        Player player = Bukkit.getPlayer(username);
        if (player != null) {
            loggedIn.put(username, true);
            player.sendMessage(ChatColor.GREEN + "Login approved. You may now use commands.");
        }
    }
    
    public void kickPlayer(String username) {
        Player player = Bukkit.getPlayer(username);
        if (player != null) {
            player.kickPlayer(ChatColor.RED + "Login denied.");
        }
    }

    private String getEmailFromAPI(String username) {
        try {
            URL url = new URL("http://infra1.laminan.my.id/getemail.php?username=" + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String email = in.readLine();
            in.close();
            return email;
        } catch (Exception e) {
            return null;
        }
    }

    private void sendVerificationEmail(Player player, String email) {
        String username = player.getName();
        String subject = "Minecraft Login Verification";
        String body = "Is this you logging in? <a href='http://infra1.laminan.my.id/verify?user=" + username + "&approve=true'>Yes</a> " +
                      "<a href='http://infra1.laminan.my.id/verify?user=" + username + "&approve=false'>No</a>";
        
        Properties props = new Properties();
        props.put("mail.smtp.host", "builderzun.serv00.net");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("noreply@builderzun.serv00.net", "Arkanataya321");
            }
        });
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@builderzun.serv00.net"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject(subject);
            message.setContent(body, "text/html");
            Transport.send(message);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Failed to send email.");
        }
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex > 1) {
            return email.charAt(0) + "****" + email.substring(atIndex);
        }
        return "****@****";
    }
}
