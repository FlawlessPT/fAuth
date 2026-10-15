package pt.flawless.fAuth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pt.flawless.fAuth.managers.AuthTitles;
import pt.flawless.fAuth.managers.AuthorizedUsers;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fAuth.managers.PostLoginManager;
import pt.flawless.fAuth.services.AuthService;
import pt.flawless.fAuth.util.Failures;
import pt.flawless.fAuth.util.MainThread;
import pt.flawless.fapi.actionbar.FActionBar;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;
import pt.flawless.fapi.sounds.FSound;

import java.util.UUID;

public class LoginCommand implements CommandExecutor {

    private final AuthService service;
    private final Plugin plugin;

    public LoginCommand(AuthService service, Plugin plugin) {
        this.service = service;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§cArgumentos em falta: /login [password]");
            FSound.fail(player);
            return true;
        }

        UUID uuid = player.getUniqueId();
        if (LoggedUsersImpl.loggedUsers.isLoggedIn(uuid)) {
            new FActionBar(player).setMessage("§cJá te encontras logado!").send();
            FSound.fail(player);
            return true;
        }

        service.verifyPassword(uuid, args[0]).whenComplete((correct, error) ->
                MainThread.run(plugin, () -> {
                    if (error != null) {
                        FConsoleLogger.sendConsoleMessage("falha no login de " + player.getName() + ": "
                                + Failures.unwrap(error), EMessageTypeEnum.ERROR);
                        if (MainThread.isSameSession(player)) {
                            FSound.fail(player);
                            player.kickPlayer("§cErro ao efetuar login. Tenta novamente!");
                        }
                        return;
                    }
                    if (!MainThread.isSameSession(player)) {
                        return;
                    }
                    if (!correct) {
                        new FActionBar(player).setMessage("§cPassword errada.").send();
                        FSound.fail(player);
                        return;
                    }
                    if (LoggedUsersImpl.loggedUsers.isLoggedIn(uuid)) {
                        return;
                    }
                    if (AuthorizedUsers.isUserAuthorized(player.getName())) {
                        AuthorizedUsers.addPlayerToWaitingFor2FA(uuid);
                        AuthTitles.titles.showTwoFactor(player);
                    } else {
                        PostLoginManager.proceedAuthentication(player);
                    }
                }));
        return true;
    }
}
