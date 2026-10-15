package pt.flawless.fAuth.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pt.flawless.fAuth.managers.AuthTitles;
import pt.flawless.fAuth.managers.AuthorizedUsers;
import pt.flawless.fAuth.managers.LoggedUsersImpl;

// TODO: Move this file to centralized place
public class PlayerQuitListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        LoggedUsersImpl.loggedUsers.removeLoggedUser(e.getPlayer().getUniqueId());
        AuthorizedUsers.removePlayerFromWaiting2FA(e.getPlayer().getUniqueId());
        AuthTitles.titles.forget(e.getPlayer().getUniqueId());
    }
}
