package pt.flawless.fAuth;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import pt.flawless.fAuth.commands.LoginCommand;
import pt.flawless.fAuth.commands.RegisterCommand;
import pt.flawless.fAuth.listeners.*;
import pt.flawless.fAuth.managers.AuthTitles;
import pt.flawless.fAuth.managers.AuthorizedUsers;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fAuth.services.AuthRepository;
import pt.flawless.fAuth.services.AuthService;
import pt.flawless.fAuth.util.AuthExecutors;
import pt.flawless.fapi.database.DatabaseService;
import pt.flawless.fapi.database.Databases;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class FAuth extends JavaPlugin {
    FConsoleLogger consoleLogger = new FConsoleLogger(this.getName());

    private ExecutorService hashExecutor;
    private boolean enabledOk;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            DatabaseService database = Databases.get();
            database.migrate(this, List.of("db/fauth/V001__create_users.sql"));

            AuthRepository repository = new AuthRepository(database);
            hashExecutor = AuthExecutors.newHashExecutor();
            AuthService service = new AuthService(repository, hashExecutor);

            getCommand("login").setExecutor(new LoginCommand(service, this));
            getCommand("register").setExecutor(new RegisterCommand(service, this));

            Bukkit.getPluginManager().registerEvents(new PlayerChatListener(this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), this);
            Bukkit.getPluginManager().registerEvents(new PlayerCommandListener(), this);
            Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
            Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
            Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(service, this), this);
            Bukkit.getPluginManager().registerEvents(new PlayerAuthListener(), this);

            enabledOk = true;
            consoleLogger.sendEnablePluginMessage();
        } catch (SQLException | RuntimeException e) {
            FConsoleLogger.sendConsoleMessage(
                    "não foi possível iniciar a autenticação: " + e.getMessage()
                            + (e.getCause() != null ? " (causa: " + e.getCause() + ")" : ""),
                    EMessageTypeEnum.ERROR);
            if (hashExecutor != null) {
                hashExecutor.shutdownNow();
                hashExecutor = null;
            }
            // Fail-closed: ninguém entra enquanto a autenticação estiver indisponível.
            HandlerList.unregisterAll(this);
            Bukkit.getPluginManager().registerEvents(new AuthUnavailableListener(), this);
        }
    }

    @Override
    public void onDisable() {
        AuthorizedUsers.clearWaiting2FA();
        LoggedUsersImpl.loggedUsers.clearLoggedUsers();
        AuthTitles.titles.clearAll();

        if (hashExecutor != null) {
            hashExecutor.shutdown();
            try {
                if (!hashExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    hashExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                hashExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (enabledOk) {
            consoleLogger.sendDisablePluginMessage();
        }
    }
}
