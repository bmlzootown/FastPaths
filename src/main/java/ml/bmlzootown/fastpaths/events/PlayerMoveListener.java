package ml.bmlzootown.fastpaths.events;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import ml.bmlzootown.fastpaths.FastPaths;
import ml.bmlzootown.fastpaths.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class PlayerMoveListener implements Listener {
    private static HashMap<UUID,Float> players = new HashMap<>();


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        String pworld = e.getTo().getWorld().getName().toLowerCase();
        Block block = e.getTo().getBlock().getRelative(BlockFace.DOWN);
        Material pblock = e.getTo().getBlock().getRelative(BlockFace.DOWN).getType();

        // HashMap<(World), (List of blocks)>
        HashMap<String, List<String>> map = ConfigManager.blocks;
        Set<String> worlds = map.keySet();

        if (!p.hasPermission("fp.use")) return;

        boolean goForIt = false;

        if (!ConfigManager.wgEnabled) {
            goForIt = true;
            //if (ConfigManager.debug) FastPaths.pl.getLogger().info(FastPaths.prefix + "WorldGuard flag check DISABLED");
        } else {
            //if (ConfigManager.debug) FastPaths.pl.getLogger().info(FastPaths.prefix + "WorldGuard flag check ENABLED");
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(p);
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            StateFlag.State state = query.queryState( (BukkitAdapter.adapt(block.getLocation())), localPlayer, FastPaths.FLAG);

            if (state == StateFlag.State.ALLOW) {
                //if (ConfigManager.debug) FastPaths.pl.getLogger().info(FastPaths.prefix + "WorldGuard flag set to ALLOW");
                goForIt = true;
            } else {
                if (players.containsKey(p.getUniqueId())) {
                    p.setWalkSpeed(players.get(p.getUniqueId()));
                    players.remove(p.getUniqueId());
                    if (ConfigManager.debug) FastPaths.pl.getLogger().info(FastPaths.prefix + "Player " + p.getName() + " no longer in allowed region -- resetting walk speed to previous value!");
                }
            }
        }

        if (goForIt) {
            if (FastPaths.containsIgnoreCase(pworld,worlds)) {
                if (FastPaths.containsIgnoreCase(pblock.name(), map.get(pworld))) {
                    if ((block.getLocation().getY() > ConfigManager.getMaxY(pworld, pblock.name())) || (block.getLocation().getY() < ConfigManager.getMinY(pworld, pblock.name()))) return;
                    if (!players.containsKey(p.getUniqueId())) {
                        players.put(p.getUniqueId(), p.getWalkSpeed());
                        p.setWalkSpeed(ConfigManager.getSpeed(pworld, pblock.name().toLowerCase()));
                        if (ConfigManager.debug) FastPaths.pl.getLogger().info(FastPaths.prefix + "Player " + p.getName() + " found in " + pworld + " on " + pblock.name() + ". Setting speed to " + ConfigManager.getSpeed(pworld, pblock.name()) + "...");
                    } else {
                        String lastBlock = e.getFrom().getBlock().getRelative(BlockFace.DOWN).getType().name().toLowerCase();
                        String newBlock = pblock.name().toLowerCase();
                        if ((!lastBlock.equalsIgnoreCase(newBlock)) && (ConfigManager.getSpeed(pworld,lastBlock) != ConfigManager.getSpeed(pworld, newBlock))) {
                            p.setWalkSpeed(ConfigManager.getSpeed(pworld, newBlock));
                            if (ConfigManager.debug) FastPaths.pl.getLogger().info(FastPaths.prefix + "Player " + p.getName() + " found in " + pworld + " on " + pblock.name() + ". Setting speed to " + ConfigManager.getSpeed(pworld, newBlock) + "...");
                        }
                    }
                    if (!ConfigManager.depleteFood(pworld, pblock.name())) {
                        //if (ConfigManager.debug) FastPaths.pl.getLogger().info(FastPaths.prefix + "Food depletion disabled...");
                        p.setSaturation(1);
                    }
                } else {
                    if (players.containsKey(p.getUniqueId())) {
                        p.setWalkSpeed(players.get(p.getUniqueId()));
                        players.remove(p.getUniqueId());
                        if (ConfigManager.debug) FastPaths.pl.getLogger().info(FastPaths.prefix + "Player " + p.getName() + " no longer on configured block -- resetting walk speed to previous value!");
                    }
                }
            } else {
                if (players.containsKey(p.getUniqueId())) {
                    p.setWalkSpeed(players.get(p.getUniqueId()));
                    players.remove(p.getUniqueId());
                    if (ConfigManager.debug) FastPaths.pl.getLogger().info(FastPaths.prefix + "Player " + p.getName() + " left " + pworld + " -- resetting walk speed to previous value!");
                }
            }
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (players.containsKey(p.getUniqueId())) {
            p.setWalkSpeed(players.get(p.getUniqueId()));
            players.remove(p.getUniqueId());
            if (ConfigManager.debug) FastPaths.pl.getLogger().info(FastPaths.prefix + "Player " + p.getName() + " left the server -- resetting walk speed to previous value!");
        }
    }

    public static void fixPlayerSpeeds() {
        Iterator it = players.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();

            UUID uuid = (UUID) pair.getKey();
            Float speed = (Float) pair.getValue();
            Player p = Bukkit.getServer().getPlayer(uuid);

            if (p.isOnline()) {
                p.setWalkSpeed(speed);
                if (ConfigManager.debug) FastPaths.pl.getLogger().info(FastPaths.prefix + "Server is stopping -- resetting everyone's walk speed to previous value!");
            }

            it.remove();
        }
    }

}
