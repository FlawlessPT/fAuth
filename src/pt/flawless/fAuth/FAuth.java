package pt.flawless.fAuth;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pt.flawless.fAuth.commands.LoginCommand;
import pt.flawless.fAuth.commands.RegisterCommand;
import pt.flawless.fAuth.database.AuthDatabaseImpl;
import pt.flawless.fAuth.listeners.*;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fAuth.utils.AuthorizedUsers;
import pt.flawless.fapi.logs.FConsoleLogger;

public class FAuth extends JavaPlugin {
    private static Plugin plugin;
    FConsoleLogger consoleLogger = new FConsoleLogger(this.getName());

    @Override
    public void onEnable() {
        plugin = this;

        consoleLogger.sendEnablePluginMessage();

        AuthDatabaseImpl.init();

        registerCommands();
        registerEvents();
    }

    @Override
    public void onDisable() {
        consoleLogger.sendDisablePluginMessage();

        AuthorizedUsers.clearWaiting2FA();
        LoggedUsersImpl.loggedUsers.clearLoggedUsers();
    }

    private void registerCommands() {
        getCommand("login").setExecutor(new LoginCommand());
        getCommand("register").setExecutor(new RegisterCommand());
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new PlayerChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerAuthListener(), this);
    }

    public static Plugin getMainPlugin() {
        return plugin;
    }
}
