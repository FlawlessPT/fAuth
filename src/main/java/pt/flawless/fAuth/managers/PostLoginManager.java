package pt.flawless.fAuth.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pt.flawless.fapi.actionbar.FActionBar;
import pt.flawless.fapi.enums.FAuthType;
import pt.flawless.fapi.events.PlayerAuthEvent.PlayerAuthEventImpl;
import pt.flawless.fapi.sounds.FSound;

/**
 * Conclui a autenticação de um jogador. Os dois métodos só podem ser chamados na thread principal
 * (tocam em Bukkit e disparam {@code PlayerAuthEvent}).
 */
public class PostLoginManager {

    /** Idempotente: se o jogador já estiver autenticado não faz nada. Só na thread principal. */
    public static void proceedAuthentication(Player player) {
        if (LoggedUsersImpl.loggedUsers.isLoggedIn(player.getUniqueId())) {
            return;
        }

        LoggedUsersImpl.loggedUsers.addLoggedUser(player.getUniqueId());
        AuthorizedUsers.removePlayerFromWaiting2FA(player.getUniqueId());

        new FActionBar(player).setMessage("§eLogado com sucesso!").send();
        FSound.success(player);
        AuthTitles.titles.clear(player);

        Bukkit.getConsoleSender().sendMessage("[fAuth] " + player.getName() + " just logged in.");

        PlayerAuthEventImpl.callPlayerAuthEvent(player, FAuthType.LOGIN);
    }

    /** Só na thread principal. */
    public static void completeRegistration(Player player) {
        LoggedUsersImpl.loggedUsers.addLoggedUser(player.getUniqueId());
        AuthTitles.titles.clear(player);

        new FActionBar(player).setMessage("§eRegistado com sucesso!").send();
        FSound.success(player);

        Bukkit.getConsoleSender().sendMessage("[fAuth] Registered user " + player.getName() + ".");

        PlayerAuthEventImpl.callPlayerAuthEvent(player, FAuthType.REGISTRATION);
    }
}
