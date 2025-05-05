package pt.flawless.fAuth.managers;

import com.mysql.jdbc.log.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// TODO: Move this file to centralized place
public class LoggedUsers {
    public final Set<UUID> loggedInPlayers = new HashSet<>();

    public Set<UUID> getLoggedInPlayers() {
        return this.loggedInPlayers;
    }

    public LoggedUsers addLoggerUser(UUID userPlayer) {
        loggedInPlayers.add(userPlayer);
        return this;
    }

    public LoggedUsers removeLoggedUser(UUID uuid) {
        loggedInPlayers.remove(uuid);
        return this;
    }

    public void clearLoggedUsers() {
        this.loggedInPlayers.clear();
    }
}