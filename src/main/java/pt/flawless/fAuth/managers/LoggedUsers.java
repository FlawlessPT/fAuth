package pt.flawless.fAuth.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// TODO: Move this file to centralized place
public class LoggedUsers {
    private Set<UUID> loggedInPlayers = new HashSet<>();

    public Set<UUID> getLoggedInPlayers() {
        return this.loggedInPlayers;
    }

    public LoggedUsers addLoggedUser(UUID userPlayer) {
        loggedInPlayers.add(userPlayer);
        return this;
    }

    public LoggedUsers removeLoggedUser(UUID uuid) {
        loggedInPlayers.remove(uuid);
        return this;
    }

    public boolean isLoggedIn(UUID uuid) {
        return loggedInPlayers.contains(uuid);
    }

    public void clearLoggedUsers() {
        this.loggedInPlayers.clear();
    }
}