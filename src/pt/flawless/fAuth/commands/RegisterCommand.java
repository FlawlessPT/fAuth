package pt.flawless.fAuth.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pt.flawless.fAuth.Main;
import pt.flawless.fAuth.listeners.PlayerJoinListener;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fapi.actionbar.FActionBar;
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
            FActionBar actionBar = new FActionBar(player);
            String password = args[0];
            String confirmPassword = args[1];

            if (!password.equalsIgnoreCase(confirmPassword)) {
                actionBar.setMessage("§cAs passwords devem corresponder!").send();
                FSound.fail(player);
                return false;
            }

            try {
                if (Main.database.isRegistered(player.getUniqueId())) {
                    actionBar.setMessage("§cUtilizador já registado!").send();
                    FSound.fail(player);
                    return false;
                }

                Main.database.registerUser(player.getUniqueId(), player.getName(), confirmPassword);

                LoggedUsersImpl.loggedUsers.addLoggedUser(player.getUniqueId());
                if (PlayerJoinListener.titleAlert != null) PlayerJoinListener.titleAlert.clear();
                actionBar.setMessage("§eRegistado com sucesso!").send();
                Bukkit.getConsoleSender().sendMessage("[fAuth] Registered user %username%.".replace("%username%", player.getName()));
                FSound.success(player);
            } catch (SQLException e) {
                player.kickPlayer("§cErro ao efetuar registo. Tenta novamente!");
                FSound.fail(player);
            }
        }
        return false;
    }
}
