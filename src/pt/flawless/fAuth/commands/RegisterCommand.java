package pt.flawless.fAuth.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pt.flawless.fAuth.Main;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fapi.sounds.FSound;

import java.sql.SQLException;

public class RegisterCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Player player = (Player) commandSender;

        if (args.length == 0 || args.length == 1 || args.length > 2) {
            player.sendMessage("§cArgumentos em falta: /register [password] [confirm_password]");
            FSound.fail(player);
        }

        if (args.length == 2) {
            String password = args[0];
            String confirmPassword = args[1];

            if (!password.equalsIgnoreCase(confirmPassword)) {
                player.sendMessage("§cAs passwords devem corresponder!");
                FSound.fail(player);
                return false;
            }

            try {
                if (Main.database.isRegistered(player.getUniqueId())) {
                    player.sendMessage("§cUtilizador já registado!");
                    FSound.fail(player);
                    return false;
                }

                Main.database.registerUser(player.getUniqueId(), player.getName(), confirmPassword);

                LoggedUsersImpl.loggedUsers.addLoggedUser(player.getUniqueId());

                player.sendMessage("§eRegistado com sucesso!");
                Bukkit.getConsoleSender().sendMessage("[fAuth] Registered user %username%.".replace("%username%", player.getName()));
                FSound.success(player);
            } catch (SQLException e) {
                player.sendMessage("§cErro ao efetuar registo.");
                FSound.fail(player);
            }
        }
        return false;
    }
}
