package pt.flawless.fAuth;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pt.flawless.fAuth.commands.LoginCommand;
import pt.flawless.fAuth.commands.RegisterCommand;
import pt.flawless.fAuth.database.AuthDatabase;
import pt.flawless.fAuth.database.AuthDatabaseImpl;
import pt.flawless.fAuth.listeners.*;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fapi.logs.FConsoleLogger;

import java.sql.SQLException;

public class Main extends JavaPlugin {
    private static Plugin plugin;

    @Override
    public void onEnable() {
        this.plugin = this;

        AuthDatabaseImpl.init();

        registerCommands();
        registerEvents();

        FConsoleLogger.sendEnablePlugin(plugin.getName());
    }

    @Override
    public void onDisable() {
        LoggedUsersImpl.loggedUsers.clearLoggedUsers();

        FConsoleLogger.sendDisablePlugin(plugin.getName());
    }

    private void registerCommands() {
        getCommand("login").setExecutor(new LoginCommand());
        getCommand("register").setExecutor(new RegisterCommand());
    }

    private void registerEvents() {
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
