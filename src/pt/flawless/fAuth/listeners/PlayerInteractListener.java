package pt.flawless.fAuth.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import pt.flawless.fAuth.managers.LoggedUsersImpl;

public class PlayerInteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!LoggedUsersImpl.loggedUsers.isLoggedIn(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }
}
