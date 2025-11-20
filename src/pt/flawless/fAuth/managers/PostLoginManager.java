package pt.flawless.fAuth.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pt.flawless.fAuth.listeners.PlayerJoinListener;
import pt.flawless.fAuth.utils.AuthorizedUsers;
import pt.flawless.fapi.actionbar.FActionBar;
import pt.flawless.fapi.enums.FAuthType;
import pt.flawless.fapi.events.PlayerAuthEvent.PlayerAuthEventImpl;
import pt.flawless.fapi.sounds.FSound;

public class PostLoginManager {
    public static void proceedAuthentication(Player player) {
        FActionBar actionBar = new FActionBar(player);

        LoggedUsersImpl.loggedUsers.addLoggedUser(player.getUniqueId());
        if (AuthorizedUsers.isPlayerWaiting2FA(player.getUniqueId())) AuthorizedUsers.removePlayerFromWaiting2FA(player.getUniqueId());

        actionBar.setMessage("§eLogado com sucesso!").send();
        FSound.success(player);

        if (PlayerJoinListener.authTitleAlert != null) PlayerJoinListener.authTitleAlert.clear();

        Bukkit.getConsoleSender().sendMessage("[fAuth] %username% just logged in.".replace("%username%", player.getName()));

        PlayerAuthEventImpl.callPlayerAuthEvent(player, FAuthType.LOGIN);
    }
}
