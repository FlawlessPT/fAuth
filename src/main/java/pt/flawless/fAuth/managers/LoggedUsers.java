package pt.flawless.fAuth.managers;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// TODO: Move this file to centralized place
public class LoggedUsers {
    private final Set<UUID> loggedInPlayers = ConcurrentHashMap.newKeySet();

    public Set<UUID> getLoggedInPlayers() {
        return Collections.unmodifiableSet(this.loggedInPlayers);
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
