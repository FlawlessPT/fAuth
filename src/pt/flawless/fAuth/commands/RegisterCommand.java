package pt.flawless.fAuth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pt.flawless.fAuth.managers.UserPlayer;
import pt.flawless.fapi.sounds.FSound;

public class RegisterCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Player player = (Player) commandSender;

        if (args.length == 0 || args.length == 1 || args.length > 2) {
            player.sendMessage("§cArgumentos em falta: /register [password] [confirm_password]");
            FSound.fail(player);
        }

        if (args.length == 2) {
            // handle register
        }
        return false;
    }
}
