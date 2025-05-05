package pt.flawless.fAuth;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pt.flawless.fAuth.commands.LoginCommand;
import pt.flawless.fAuth.commands.RegisterCommand;
import pt.flawless.fAuth.database.AuthDatabase;
import pt.flawless.fAuth.listeners.PlayerMoveListener;
import pt.flawless.fAuth.listeners.PlayerQuitListener;
import pt.flawless.fAuth.listeners.PlayerSpawnListener;

import java.sql.SQLException;

public class Main extends JavaPlugin {
    public static Plugin plugin;
    public static AuthDatabase database;

    private void loadDatabase() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        try {
            Class.forName("org.sqlite.JDBC");
            database = new AuthDatabase(getDataFolder());
            database.connect();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("SQL Exception - Error creating database!");
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            Bukkit.getConsoleSender().sendMessage("Class Not Found- Error creating database!");
        }
    }

    private void registerCommands() {
        getCommand("login").setExecutor(new LoginCommand());
        getCommand("register").setExecutor(new RegisterCommand());
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSpawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
    }

    @Override
    public void onLoad() {
        //reloadConfig();
    }

    @Override
    public void onEnable() {
        //saveDefaultConfig();
        plugin = this;
        loadDatabase();
        registerCommands();
        registerEvents();

        ConsoleCommandSender b = Bukkit.getConsoleSender();
        b.sendMessage("§e");
        b.sendMessage("§e[fAuth] Plugin ativado com sucesso!");
        b.sendMessage("§e");
    }

    @Override
    public void onDisable() {
        ConsoleCommandSender b = Bukkit.getConsoleSender();
        b.sendMessage("§c");
        b.sendMessage("§c[fAuth] Plugin desativado com sucesso!");
        b.sendMessage("§c");

    }
}
