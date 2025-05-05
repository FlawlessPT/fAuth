package pt.flawless.fAuth.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

// TODO: Move this file to centralized place
public class PlayerSpawnListener implements Listener {
    @EventHandler
    public void onPlayerSpawnEvent(PlayerSpawnLocationEvent playerSpawnListener) {
        playerSpawnListener.setSpawnLocation(new Location(Bukkit.getServer().getWorld("world"), 137.517, 70, -46.536));
        // show message on join
    }
}
