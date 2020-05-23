package ml.bmlzootown.fastpaths;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import ml.bmlzootown.fastpaths.cmds.FastPathsCommander;
import ml.bmlzootown.fastpaths.events.PlayerMoveListener;
import ml.bmlzootown.fastpaths.utils.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


import java.io.File;
import java.util.List;
import java.util.Set;

public class FastPaths extends JavaPlugin {
    public static Plugin pl;
    public static PluginManager pm;
    public static WorldGuardPlugin wgp;
    public static String prefix = ChatColor. BLUE + "[FastPaths] " + ChatColor.RESET;

    public static final StateFlag FLAG = new StateFlag("fast-path", true);

    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(FLAG);
        } catch (FlagConflictException e) {
            e.printStackTrace();
        }
    }

    public void onEnable() {
        pl = this;
        pm = getServer().getPluginManager();
        wgp = getWorldGuard();

        //Config setup
        File f = new File(getDataFolder(), "config.yml");
        if (!f.exists()) {
            if (ConfigManager.debug) FastPaths.pl.getLogger().info(prefix + "No config found -- generating!");
            ConfigManager.setConfigDefaults();
        }
        ConfigManager.initialize();

        //Disable plugin if config enabled set to false
        if (!ConfigManager.isEnabled()) {
            if (ConfigManager.debug) FastPaths.pl.getLogger().info(prefix + "Enabled set to false in config -- disabling plugin!");
            getServer().getPluginManager().disablePlugin(this);
        }

        //Register events/commands
        if (ConfigManager.debug) FastPaths.pl.getLogger().info(prefix + "Registering any events/commands...");
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        getCommand("fastpaths").setExecutor(new FastPathsCommander());
    }

    public void onDisable() {
        PlayerMoveListener.fixPlayerSpeeds();
    }

    public static void reloadCf() {
        pl.reloadConfig();
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin p = pm.getPlugin("WorldGuard");

        if (p == null || !(p instanceof WorldGuardPlugin)) {
            return null;
        }

        return (WorldGuardPlugin) p;
    }

    public static boolean containsIgnoreCase(String str, List<String> list) {
        try {
            for (String i : list) {
                if (i.equalsIgnoreCase(str))
                    return true;
            }
        } catch (NullPointerException e) {
            return false;
        }
        return false;
    }

    public static boolean containsIgnoreCase(String str, Set<String> list) {
        try {
            for (String i : list) {
                if (i.equalsIgnoreCase(str))
                    return true;
            }
        } catch (NullPointerException e) {
            return false;
        }
        return false;
    }

}
