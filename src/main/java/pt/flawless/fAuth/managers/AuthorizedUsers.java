package pt.flawless.fAuth.managers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AuthorizedUsers {
    public static final Map<String, String> ADMIN_CODES = Map.of(
            "FlawlessYasuo", "1234"
    );

    public static final Set<UUID> waitingFor2FA = new HashSet<>();

    public static String getCode(String playerName) {
        return ADMIN_CODES.get(playerName);
    }

    public static boolean isUserAuthorized(String playerName) {
        return ADMIN_CODES.containsKey(playerName);
    }

    public static void addPlayerToWaitingFor2FA(UUID uuid) {
        waitingFor2FA.add(uuid);
    }

    public static boolean isPlayerWaiting2FA(UUID uuid) {
        return waitingFor2FA.contains(uuid);
    }

    public static void removePlayerFromWaiting2FA(UUID uuid) {
        waitingFor2FA.remove(uuid);
    }

    public static void clearWaiting2FA() {
        waitingFor2FA.clear();
    }
}
