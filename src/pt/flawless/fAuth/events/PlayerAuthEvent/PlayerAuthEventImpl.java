package pt.flawless.fAuth.events.PlayerAuthEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerAuthEventImpl {
    public static void callPlayerAuthEvent(Player player) {
        PlayerAuthEvent authEvent = new PlayerAuthEvent(player);
        Bukkit.getServer().getPluginManager().callEvent(authEvent);
    }
}
