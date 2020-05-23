package ml.bmlzootown.fastpaths.utils;

import ml.bmlzootown.fastpaths.FastPaths;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class ConfigManager {
    private static Plugin pl = FastPaths.pl;

    // HashMap<World, Blocks>
    public static HashMap<String, List<String>> blocks = new HashMap<>();
    public static FileConfiguration cf;
    public static boolean debug;

    public static boolean wgEnabled = false;

    public static void setConfigDefaults() {
        FileConfiguration config = pl.getConfig();
        config.options().header("Speed is a float value between -1.0 and 1.0. The WorldGuard flag is 'fast-path'. ");
        config.addDefault("plugin-enabled", true);
        config.addDefault("debug", false);
        config.addDefault("worldguard-enabled", false);
        config.addDefault("worlds.lobby.blocks.bedrock.enabled", true);
        config.addDefault("worlds.lobby.blocks.bedrock.minY", 5);
        config.addDefault("worlds.lobby.blocks.bedrock.maxY", 200);
        config.addDefault("worlds.lobby.blocks.bedrock.speed", 0.3);
        config.addDefault("worlds.lobby.blocks.bedrock.deplete-food", false);
        config.options().copyDefaults(true);
        pl.saveConfig();
    }

    public static void initialize() {
        cf = pl.getConfig();
        debug = debug();

        if (ConfigManager.debug) FastPaths.pl.getLogger().info(FastPaths.prefix + "Initializing config...");
        for (String world : ConfigManager.getWorlds()) {
            List<String> bls = new ArrayList<>();
            for (String block : ConfigManager.getBlocks(world)) {
                if (ConfigManager.isBlockEnabled(world, block)) {
                    bls.add(block);
                }
            }
            blocks.put(world.toLowerCase(), bls);
        }
        wgEnabled = wgFlag();

        if (wgEnabled) {
            if (ConfigManager.debug()) FastPaths.pl.getLogger().info(FastPaths.prefix + "WorldGuard flag check ENABLED");
        } else {
            if (ConfigManager.debug()) FastPaths.pl.getLogger().info(FastPaths.prefix + "WorldGuard flag check DISABLED");
        }
    }

    // Returns a list of world names
    private static List<String> getWorlds() {
        Set<String> keys = cf.getConfigurationSection("worlds").getKeys(false);
        List<String> worlds = new ArrayList<>();
        worlds.addAll(keys);
        return worlds;
    }

    // Returns a list of blocks for a specified world
    private static List<String> getBlocks(String world) {
        Set<String> keys = cf.getConfigurationSection("worlds." + world.toLowerCase() + ".blocks").getKeys(false);
        List<String> blocks = new ArrayList<>();
        blocks.addAll(keys);
        return blocks;
    }

    // Is plugin enabled?
    public static boolean isEnabled() { return cf.getBoolean("plugin-enabled"); }

    // Is block enabled in specified world?
    private static boolean isBlockEnabled(String world, String block) { return cf.getBoolean("worlds." + world.toLowerCase() + ".blocks." + block.toLowerCase() + ".enabled"); }

    // Deplete food while walking on block in world?
    public static boolean depleteFood(String world, String block) { return cf.getBoolean("worlds." + world.toLowerCase() + ".blocks." + block.toLowerCase() + ".deplete-food"); }

    // Speed for block in world
    public static Float getSpeed(String world, String block) { return (float) cf.getDouble("worlds." + world.toLowerCase() + ".blocks." + block.toLowerCase() + ".speed"); }

    // Minimum Y for block in world
    public static Integer getMinY(String world, String block) { return cf.getInt("worlds." + world.toLowerCase() + ".blocks." + block.toLowerCase() + ".minY"); }

    // Maximum Y for block in world
    public static Integer getMaxY(String world, String block) { return cf.getInt("worlds." + world.toLowerCase() + ".blocks." + block.toLowerCase() + ".maxY"); }

    // Check for WorldGuard flag?
    private static boolean wgFlag() { return cf.getBoolean("worldguard-enabled"); }

    // Should we spam the console with fun debug messages?
    private static boolean debug() { return cf.getBoolean("debug"); }

    public static void reload() {
        FastPaths.reloadCf();
        initialize();
    }
}
