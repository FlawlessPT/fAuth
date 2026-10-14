package pt.flawless.fAuth.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pt.flawless.fapi.events.PlayerAuthEvent.PlayerAuthEvent;

public class PlayerAuthListener implements Listener {
    @EventHandler
    public void onPlayerAuth(PlayerAuthEvent e) {
        Player player = e.getPlayer();
        player.sendMessage("§e ");
        player.sendMessage("§eBem vindo ao servidor!");
        player.sendMessage("§eSe tiveres dúvidas contacta a nossa §fSTAFF§f.");
        player.sendMessage("§e ");
        player.sendMessage("§eDesejámos-te um bom jogo!");
        player.sendMessage("§e ");
    }
}
