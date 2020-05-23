package ml.bmlzootown.fastpaths.cmds;

import ml.bmlzootown.fastpaths.FastPaths;
import ml.bmlzootown.fastpaths.utils.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FastPathsCommander implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player)sender;
            if (p.hasPermission("fp.admin") && args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    p.sendMessage(FastPaths.prefix + "Config reloaded!");
                    if (ConfigManager.debug) FastPaths.pl.getLogger().info(FastPaths.prefix + "Config reloaded...");
                    ConfigManager.reload();
                }
            }
            if (args.length == 0) {
                if (p.hasPermission("fp.admin")) {
                    p.sendMessage(FastPaths.prefix + "Commands:");
                    p.sendMessage("/fp reload");
                } else {
                    p.sendMessage(FastPaths.prefix + "You don't have permission to do that!");
                }
            }
        }

        return false;
    }
}
