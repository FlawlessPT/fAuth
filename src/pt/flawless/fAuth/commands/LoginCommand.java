package pt.flawless.fAuth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pt.flawless.fAuth.managers.LoggedUsersImpl;
import pt.flawless.fAuth.managers.UserPlayer;
import pt.flawless.fapi.sounds.FSound;

public class LoginCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Player player = (Player) commandSender;

        if (args.length == 0 || args.length > 1) {
            player.sendMessage("§cArgumentos em falta: /login [password]");
            FSound.fail(player);
        }

        if (args.length == 1) {
            // check login
            //UserPlayer userPlayer = new UserPlayer().setUuid(player.getUniqueId());
            LoggedUsersImpl.loggedUsers.addLoggerUser(player.getUniqueId());
        }
        return false;
    }
}
