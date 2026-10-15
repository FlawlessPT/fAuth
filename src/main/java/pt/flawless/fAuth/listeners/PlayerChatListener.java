package pt.flawless.fAuth.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import pt.flawless.fAuth.managers.AuthorizedUsers;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fAuth.managers.PostLoginManager;
import pt.flawless.fAuth.util.MainThread;
import pt.flawless.fapi.sounds.FSound;

import java.util.UUID;

public class PlayerChatListener implements Listener {

    private final Plugin plugin;

    public PlayerChatListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        ChatGate.Decision decision = ChatGate.decide(
                LoggedUsersImpl.loggedUsers.isLoggedIn(uuid),
                AuthorizedUsers.isPlayerWaiting2FA(uuid));

        switch (decision) {
            case ALLOW -> {
            }
            case REQUIRE_LOGIN -> {
                // O código 2FA nunca é comparado para quem não está à espera dele.
                event.setCancelled(true);
                MainThread.run(plugin, () -> {
                    if (!player.isOnline()) {
                        return;
                    }
                    player.sendMessage("§cEfetua login primeiro: /login [password]");
                    FSound.fail(player);
                });
            }
            case CHECK_CODE -> {
                event.setCancelled(true);
                boolean correct = AuthorizedUsers.matchesCode(player.getName(), event.getMessage());
                MainThread.run(plugin, () -> {
                    if (!player.isOnline()) {
                        return;
                    }
                    if (correct) {
                        if (AuthorizedUsers.isPlayerWaiting2FA(uuid)) {
                            PostLoginManager.proceedAuthentication(player);
                        }
                    } else {
                        player.sendMessage("§cCódigo errado!");
                        FSound.fail(player);
                    }
                });
            }
        }
    }
}
