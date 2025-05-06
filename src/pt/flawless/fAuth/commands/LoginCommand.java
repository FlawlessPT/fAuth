package pt.flawless.fAuth.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pt.flawless.fAuth.Main;
import pt.flawless.fAuth.database.AuthDatabaseImpl;
import pt.flawless.fAuth.events.PlayerAuthEvent.PlayerAuthEventImpl;
import pt.flawless.fAuth.listeners.PlayerJoinListener;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fapi.actionbar.FActionBar;
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
            FActionBar actionBar = new FActionBar(player);

            if (LoggedUsersImpl.loggedUsers.getLoggedInPlayers().contains(player.getUniqueId())) {
                actionBar.setMessage("§cJá te encontras logado!").send();
                FSound.fail(player);

                return false;
            }

            try {
                String password = args[0];
                Boolean isCorrectPassword = AuthDatabaseImpl.database.verifyPassword(player.getUniqueId(), password);

                if (isCorrectPassword) {
                    LoggedUsersImpl.loggedUsers.addLoggedUser(player.getUniqueId());

                    actionBar.setMessage("§eLogado com sucesso!").send();
                    if (PlayerJoinListener.titleAlert != null) PlayerJoinListener.titleAlert.clear();
                    Bukkit.getConsoleSender().sendMessage("[fAuth] %username% just logged in.".replace("%username%", player.getName()));
                    FSound.success(player);
                    PlayerAuthEventImpl.callPlayerAuthEvent(player);
                } else {
                    actionBar.setMessage("§cPassword errada.").send();
                    FSound.fail(player);
                }
            } catch (SQLException e) {
                FSound.fail(player);
                player.kickPlayer("§cErro ao efetuar login. Tenta novamente!");
            }
        }
        return false;
    }
}
