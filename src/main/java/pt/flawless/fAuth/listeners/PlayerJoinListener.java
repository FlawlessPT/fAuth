package pt.flawless.fAuth.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import pt.flawless.fAuth.managers.AuthTitles;
import pt.flawless.fAuth.managers.AuthorizedUsers;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fAuth.services.AuthService;
import pt.flawless.fAuth.util.Failures;
import pt.flawless.fAuth.util.MainThread;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;

// TODO: Move this file to centralized place
public class PlayerJoinListener implements Listener {

    private final AuthService service;
    private final Plugin plugin;

    public PlayerJoinListener(AuthService service, Plugin plugin) {
        this.service = service;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        service.isRegistered(player.getUniqueId()).whenComplete((registered, error) ->
                MainThread.run(plugin, () -> {
                    if (!MainThread.isSameSession(player)) {
                        return;
                    }
                    if (error != null) {
                        FConsoleLogger.sendConsoleMessage("falha ao validar o estado do login de "
                                + player.getName() + ": " + Failures.unwrap(error), EMessageTypeEnum.ERROR);
                        player.kickPlayer("§cErro ao validar o estado do teu login.");
                        return;
                    }
                    if (LoggedUsersImpl.loggedUsers.isLoggedIn(player.getUniqueId())
                            || AuthorizedUsers.isPlayerWaiting2FA(player.getUniqueId())) {
                        return;
                    }
                    if (registered) {
                        AuthTitles.titles.showLogin(player);
                    } else {
                        AuthTitles.titles.showRegister(player);
                    }
                }));
    }
}
