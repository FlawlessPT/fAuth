package pt.flawless.fAuth.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pt.flawless.fAuth.database.AuthDatabaseImpl;
import pt.flawless.fapi.title.FTitle;

import java.sql.SQLException;

// TODO: Move this file to centralized place
public class PlayerJoinListener implements Listener {
    public static FTitle authTitleAlert;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        
        try {
            Boolean isRegistered = AuthDatabaseImpl.database.isRegistered(player.getUniqueId());

            authTitleAlert = new FTitle().setPlayer(player).setTime(999);
            String message;

            if (isRegistered) {
                message = "§cPara prosseguir, efetua login: /login [password]§c ";
                authTitleAlert.setMessage(message);
            } else {
                message = "§cPara prosseguir, regista-te: /register [password] [password]§c ";
                authTitleAlert.setMessage(message);
            }

            //player.sendMessage(message);
            authTitleAlert.send();
        } catch (SQLException ex) {
            player.kickPlayer("§cErro ao validar o estado do teu login.");
        }
    }
}
