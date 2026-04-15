package com.aynclub.akilleffect.commands;

import com.aynclub.akilleffect.Main;
import com.aynclub.akilleffect.utils.User;
import com.aynclub.akilleffect.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Help implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 1 || "menu".equalsIgnoreCase(args[0])) {
            Main.getManager().buildInventory(User.getUser(player.getUniqueId())).open(player);
            return true;
        }

        if ("help".equalsIgnoreCase(args[0])) {
            player.sendMessage(Utils.colorize(String.valueOf(Utils.gfc("config", "help-command-message"))));
            return true;
        }

        player.sendMessage(Main.PREFIX + " Unknown command. Use /killeffect or /killeffect help.");
        return true;
    }
}
