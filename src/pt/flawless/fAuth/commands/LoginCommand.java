package pt.flawless.fAuth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pt.flawless.fAuth.database.AuthDatabaseImpl;
import pt.flawless.fAuth.listeners.PlayerJoinListener;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fAuth.managers.PostLoginManager;
import pt.flawless.fAuth.utils.AuthorizedUsers;
import pt.flawless.fapi.actionbar.FActionBar;
import pt.flawless.fapi.sounds.FSound;

import java.sql.SQLException;

public class LoginCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Player player = (Player) commandSender;

        if (args.length != 1) {
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
                boolean isCorrectPassword = AuthDatabaseImpl.database.verifyPassword(player.getUniqueId(), password);

                if (isCorrectPassword) {
                    // Handle 2FA authentication
                    if (AuthorizedUsers.isUserAuthorized(player.getName())) {
                        PlayerJoinListener.authTitleAlert.clear();
                        PlayerJoinListener.authTitleAlert.setMessage("§eAdmin detetado. Por favor insere o teu código 2FA:");
                        PlayerJoinListener.authTitleAlert.send();

                        AuthorizedUsers.addPlayerToWaitingFor2FA(player.getUniqueId());

                        return false;
                    }

                    PostLoginManager.proceedAuthentication(player);
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
