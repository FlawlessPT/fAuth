package pt.flawless.fAuth.managers;

import org.bukkit.entity.Player;

import java.util.UUID;

public class UserPlayer {
    private UUID uuid;
    private Boolean isLoggedIn = false;

    public void setLoggedIn(Boolean loggedIn) {
        this.isLoggedIn = loggedIn;
    }

    public Boolean getLoggedIn() {
        return this.isLoggedIn;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UserPlayer setUuid(UUID uuid) {
        this.uuid = uuid;

        return this;
    }
}
