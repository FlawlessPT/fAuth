package pt.flawless.fAuth.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fAuth.managers.PostLoginManager;
import pt.flawless.fAuth.utils.AuthorizedUsers;
import pt.flawless.fapi.sounds.FSound;

public class PlayerChatListener implements Listener {
    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent playerChatEvent) {
        Player player = playerChatEvent.getPlayer();

        if (LoggedUsersImpl.loggedUsers.isLoggedIn(player.getUniqueId()) && !AuthorizedUsers.isPlayerWaiting2FA(player.getUniqueId())) {
            return;
        }

        playerChatEvent.setCancelled(true);

        if (playerChatEvent.getMessage().equalsIgnoreCase(AuthorizedUsers.getCode(player.getName()))) {
            PostLoginManager.proceedAuthentication(player);
        } else {
            player.sendMessage("§cCódigo errado!");
            FSound.fail(player);
        }
    }
}
