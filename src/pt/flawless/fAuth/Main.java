package pt.flawless.fAuth;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pt.flawless.fAuth.commands.LoginCommand;
import pt.flawless.fAuth.commands.RegisterCommand;

public class Main extends JavaPlugin {
    public static Plugin plugin;

    private void registerCommands() {
        getCommand("login").setExecutor(new LoginCommand());
        getCommand("register").setExecutor(new RegisterCommand());
    }

    private void registerEvents() {
    }

    @Override
    public void onLoad() {
        //reloadConfig();
    }

    @Override
    public void onEnable() {
        //saveDefaultConfig();
        plugin = this;
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
