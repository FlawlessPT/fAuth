package pt.flawless.fAuth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pt.flawless.fAuth.exceptions.UserAlreadyRegisteredException;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fAuth.managers.PostLoginManager;
import pt.flawless.fAuth.services.AuthService;
import pt.flawless.fAuth.util.Failures;
import pt.flawless.fAuth.util.MainThread;
import pt.flawless.fapi.actionbar.FActionBar;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;
import pt.flawless.fapi.sounds.FSound;

public class RegisterCommand implements CommandExecutor {

    private final AuthService service;
    private final Plugin plugin;

    public RegisterCommand(AuthService service, Plugin plugin) {
        this.service = service;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage("§cArgumentos em falta: /register [password] [confirm_password]");
            FSound.fail(player);
            return true;
        }

        // Antes de calcular qualquer hash.
        if (LoggedUsersImpl.loggedUsers.isLoggedIn(player.getUniqueId())) {
            new FActionBar(player).setMessage("§cJá te encontras logado!").send();
            FSound.fail(player);
            return true;
        }

        String password = args[0];
        if (!password.equals(args[1])) {
            new FActionBar(player).setMessage("§cAs passwords devem corresponder!").send();
            FSound.fail(player);
            return true;
        }

        service.register(player.getUniqueId(), player.getName(), password).whenComplete((ignored, error) ->
                MainThread.run(plugin, () -> {
                    Throwable cause = error == null ? null : Failures.unwrap(error);
                    if (cause != null && !(cause instanceof UserAlreadyRegisteredException)) {
                        FConsoleLogger.sendConsoleMessage("falha no registo de " + player.getName() + ": "
                                + cause, EMessageTypeEnum.ERROR);
                    }
                    if (!MainThread.isSameSession(player)) {
                        return;
                    }
                    if (cause == null) {
                        PostLoginManager.completeRegistration(player);
                    } else if (cause instanceof UserAlreadyRegisteredException) {
                        new FActionBar(player).setMessage("§cUtilizador já registado!").send();
                        FSound.fail(player);
                    } else {
                        FSound.fail(player);
                        player.kickPlayer("§cErro ao efetuar registo. Tenta novamente!");
                    }
                }));
        return true;
    }
}
