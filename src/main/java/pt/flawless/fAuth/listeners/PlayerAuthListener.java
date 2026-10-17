package pt.flawless.fAuth.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pt.flawless.fAuth.FAuth;
import pt.flawless.fapi.events.PlayerAuthEvent.PlayerAuthEvent;

public class PlayerAuthListener implements Listener {
    @EventHandler
    public void onPlayerAuth(PlayerAuthEvent e) {
        Player player = e.getPlayer();
        for (String line : JavaPlugin.getPlugin(FAuth.class).getConfig().getStringList("Mensagens.BemVindo")) {
            player.sendMessage(line.replace("{player}", player.getName()).replace("&", "§"));
        }
    }
}
