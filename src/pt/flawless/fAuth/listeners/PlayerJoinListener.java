package pt.flawless.fAuth.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import pt.flawless.fAuth.Main;
import pt.flawless.fAuth.managers.LoggedUsersImpl;

import java.sql.SQLException;

// TODO: Move this file to centralized place
public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        try {
            Boolean isRegistered = Main.database.isRegistered(e.getPlayer().getUniqueId());

            if (isRegistered) {
                e.getPlayer().sendMessage("\n§cPara prosseguir, efetua login:\n§c/login [password]\n");
            } else {
                e.getPlayer().sendMessage("\n§cPara prosseguir, regista-te:\n§c/login [password]\n");
            }
        } catch (SQLException ex) {
            e.getPlayer().kickPlayer("§cErro ao validar o estado do teu login.");
        }
    }
}
