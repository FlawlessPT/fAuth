package pt.flawless.fAuth.commands;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pt.flawless.fAuth.Main;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fapi.sounds.FSound;

import java.sql.SQLException;

public class LoginCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Player player = (Player) commandSender;

        if (args.length == 0 || args.length > 1) {
            player.sendMessage("§cArgumentos em falta: /login [password]");
            FSound.fail(player);
        }

        if (args.length == 1) {
            if (LoggedUsersImpl.loggedUsers.getLoggedInPlayers().contains(player.getUniqueId())) {
                player.sendMessage("§cJá te encontras logado!");
                FSound.fail(player);
            }

            try {
                String password = args[0];
                Boolean isCorrectPassword = Main.database.verifyPassword(player.getUniqueId(), password);

                if (isCorrectPassword) {
                    LoggedUsersImpl.loggedUsers.addLoggedUser(player.getUniqueId());

                    player.sendMessage("§eLogado com sucesso!");
                    Bukkit.getConsoleSender().sendMessage("[fAuth] %username% just logged in.".replace("%username%", player.getName()));
                    FSound.success(player);
                } else {
                    player.sendMessage("§cPassword errada.");
                    FSound.sendSound(player, Sound.ANVIL_BREAK);
                }
            } catch (SQLException e) {
                player.sendMessage("§cErro ao efetuar registo.");
                FSound.fail(player);
            }
        }
        return false;
    }
}
