package pt.flawless.fAuth.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fAuth.managers.UserPlayer;

public class PlayerMoveListener implements Listener {
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        if (LoggedUsersImpl.loggedUsers.getLoggedInPlayers().contains(e.getPlayer().getUniqueId())) {
            return;
        }

        // Check if the player actually moved to a new block
        if (e.getFrom().getX() != e.getTo().getX() ||
                e.getFrom().getY() != e.getTo().getY() ||
                e.getFrom().getZ() != e.getTo().getZ()) {

            // Cancel movement
            e.setTo(e.getFrom());
        }
    }
}
