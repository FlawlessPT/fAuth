package pt.flawless.fAuth.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fapi.sounds.FSound;

import java.util.Locale;
import java.util.Set;

/** Bloqueia todos os comandos até ao login; só /login e /register (e aliases) passam. */
public class PlayerCommandListener implements Listener {

    private static final Set<String> ALLOWED = Set.of("login", "logar", "register", "registar");

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (LoggedUsersImpl.loggedUsers.isLoggedIn(e.getPlayer().getUniqueId())) {
            return;
        }
        if (ALLOWED.contains(commandName(e.getMessage()))) {
            return;
        }
        e.setCancelled(true);
        e.getPlayer().sendMessage("§cEfetua login primeiro: /login [password]");
        FSound.fail(e.getPlayer());
    }

    /** "/FAuth:Login a b" -> "login". */
    static String commandName(String message) {
        String name = message.trim().split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        int colon = name.indexOf(':');
        if (colon >= 0) {
            if (!name.substring(0, colon).equals("fauth")) {
                return name; // outro plugin: nunca na lista de permitidos
            }
            name = name.substring(colon + 1);
        }
        return name;
    }
}
