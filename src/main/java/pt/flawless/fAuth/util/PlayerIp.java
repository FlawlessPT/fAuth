package pt.flawless.fAuth.util;

import org.bukkit.entity.Player;

public final class PlayerIp {

    private PlayerIp() {
    }

    public static String of(Player player) {
        return player.getAddress().getAddress().getHostAddress();
    }
}
