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
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public class FalconForce extends JavaPlugin implements CommandExecutor {
    private final Set<UUID> verifiedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        getCommand("facon").setExecutor(this);
        getCommand("login").setExecutor(this);
        getServer().getPluginManager().registerEvents(new CommandBlocker(this), this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && !command.getName().equalsIgnoreCase("faconaprolog")) {
            sender.sendMessage(ChatColor.RED + "Unknown command.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("facon")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String email = fetchEmail(player.getName());
                if (email == null) {
                    player.sendMessage(ChatColor.RED + "Error fetching your email. Contact admin.");
                    return true;
                }
                
                String maskedEmail = maskEmail(email);
                player.sendMessage(ChatColor.GREEN + "We sent you an email to " + maskedEmail);
                sendVerificationEmail(player, email);
            }
        } else if (command.getName().equalsIgnoreCase("faconaprolog")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: faconaprolog <username> <code>");
                return true;
            }
            
            String username = args[0];
            String code = args[1];
            Player player = Bukkit.getPlayer(username);
            if (player != null) {
                verifyPlayer(player);
            }
        }
        return true;
    }

    public void verifyPlayer(Player player) {
        verifiedPlayers.add(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "You are now verified!");
    }

    private String fetchEmail(String username) {
        try {
            URL url = new URL("https://infra1.laminan.my.id/getemail.php?username=" + username);
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
        String verificationLink = "https://infra1.laminan.my.id/verify?user=" + player.getUniqueId();
        String message = "Is this you? Click below to verify:\n" + verificationLink;
        
        final String username = "noreply@builderzun.serv00.net";
        final String password = "Arkanataya321";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "builderzun.serv00.net");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(username));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            mimeMessage.setSubject("FalconForce Verification");
            mimeMessage.setText(message);

            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return "****" + email.substring(atIndex);
        return email.charAt(0) + "****" + email.substring(atIndex);
    }
}
