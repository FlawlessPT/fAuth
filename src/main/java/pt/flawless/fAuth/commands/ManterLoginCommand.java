package pt.flawless.fAuth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fAuth.services.AuthService;
import pt.flawless.fAuth.util.Failures;
import pt.flawless.fAuth.util.MainThread;
import pt.flawless.fAuth.util.PlayerIp;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;
import pt.flawless.fapi.sounds.FSound;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ManterLoginCommand implements CommandExecutor {

    private static final long DAY_MILLIS = 86_400_000L;

    private final AuthService service;
    private final Plugin plugin;

    public ManterLoginCommand(AuthService service, Plugin plugin) {
        this.service = service;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        if (!plugin.getConfig().getBoolean("Sessao.Ativa", true)) {
            player.sendMessage("§cA sessão guardada está desativada.");
            FSound.fail(player);
            return true;
        }

        UUID uuid = player.getUniqueId();
        if (!LoggedUsersImpl.loggedUsers.isLoggedIn(uuid)) {
            player.sendMessage("§cEfetua login primeiro: /login [password]");
            FSound.fail(player);
            return true;
        }

        if (args.length != 0) {
            player.sendMessage("Uso: /manterlogin");
            FSound.fail(player);
            return true;
        }

        int days = plugin.getConfig().getInt("Sessao.Dias", 7);
        String ip = PlayerIp.of(player);
        long now = System.currentTimeMillis();

        service.hasValidSession(uuid, ip, now).thenCompose(valid -> {
            if (valid) {
                return service.disableRemember(uuid).thenApply(v -> false);
            }
            return service.enableRemember(uuid, ip, now + days * DAY_MILLIS).thenApply(v -> true);
        }).whenComplete((enabled, error) -> MainThread.run(plugin, () -> {
            if (error != null) {
                FConsoleLogger.sendConsoleMessage("falha ao guardar a sessão de " + player.getName() + ": "
                        + Failures.unwrap(error), EMessageTypeEnum.ERROR);
                if (MainThread.isSameSession(player)) {
                    player.sendMessage("§cErro ao guardar a sessão. Tenta novamente.");
                    FSound.fail(player);
                }
                return;
            }
            if (!MainThread.isSameSession(player)) {
                return;
            }
            player.sendMessage(enabled
                    ? "§aSessão guardada por " + days + " dias neste IP."
                    : "§eSessão guardada desligada.");
        }));
        return true;
    }
}
