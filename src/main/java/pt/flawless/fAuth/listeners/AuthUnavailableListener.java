package pt.flawless.fAuth.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/** Fail-closed: registado sozinho quando a autenticação não arrancou; recusa todas as entradas. */
public class AuthUnavailableListener implements Listener {
    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                "§cSistema de autenticação indisponível. Tenta mais tarde.");
    }
}
