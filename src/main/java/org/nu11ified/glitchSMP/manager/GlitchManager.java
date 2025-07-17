package org.nu11ified.glitchSMP.manager;

import org.bukkit.entity.Player;
import org.nu11ified.glitchSMP.GlitchSMP;
import org.nu11ified.glitchSMP.glitch.Glitch;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages glitches for all players on the server.
 */
public class GlitchManager {
    private final GlitchSMP plugin;
    
    // Maximum number of glitches a player can equip
    private static final int MAX_EQUIPPED_GLITCHES = 2;
    
    // Map of player UUIDs to their equipped glitches
    private final Map<UUID, List<Glitch>> equippedGlitches = new ConcurrentHashMap<>();
    
    // Map of player UUIDs to their owned glitches
    private final Map<UUID, Set<Glitch>> ownedGlitches = new ConcurrentHashMap<>();
    
    // Map of active glitches and their scheduled deactivation tasks
    private final Map<UUID, Map<UUID, Integer>> activeGlitchTasks = new ConcurrentHashMap<>();
    
    /**
     * Constructor for GlitchManager
     * 
     * @param plugin The main plugin instance
     */
    public GlitchManager(GlitchSMP plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Gives a glitch to a player
     * 
     * @param player The player to give the glitch to
     * @param glitch The glitch to give
     * @return true if the player didn't already have the glitch, false otherwise
     */
    public boolean giveGlitch(Player player, Glitch glitch) {
        UUID playerUUID = player.getUniqueId();
        
        // Initialize collections if they don't exist
        ownedGlitches.computeIfAbsent(playerUUID, k -> new HashSet<>());
        
        // Check if player already has this type of glitch
        boolean alreadyOwned = false;
        for (Glitch ownedGlitch : ownedGlitches.get(playerUUID)) {
            if (ownedGlitch.getClass().equals(glitch.getClass())) {
                alreadyOwned = true;
                break;
            }
        }
        
        if (!alreadyOwned) {
            ownedGlitches.get(playerUUID).add(glitch);
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes a glitch from a player
     * 
     * @param player The player to remove the glitch from
     * @param glitch The glitch to remove
     * @return true if the player had the glitch and it was removed, false otherwise
     */
    public boolean removeGlitch(Player player, Glitch glitch) {
        UUID playerUUID = player.getUniqueId();
        
        if (!ownedGlitches.containsKey(playerUUID)) {
            return false;
        }
        
        // If the glitch is equipped, unequip it first
        if (isGlitchEquipped(player, glitch)) {
            unequipGlitch(player, glitch);
        }
        
        return ownedGlitches.get(playerUUID).remove(glitch);
    }
    
    /**
     * Equips a glitch for a player
     * 
     * @param player The player to equip the glitch for
     * @param glitch The glitch to equip
     * @return true if the glitch was equipped, false if the player doesn't own the glitch or has max glitches equipped
     */
    public boolean equipGlitch(Player player, Glitch glitch) {
        UUID playerUUID = player.getUniqueId();
        
        // Initialize collections if they don't exist
        equippedGlitches.computeIfAbsent(playerUUID, k -> new ArrayList<>());
        
        // Check if player owns the glitch
        if (!ownedGlitches.containsKey(playerUUID) || !ownedGlitches.get(playerUUID).contains(glitch)) {
            return false;
        }
        
        // Check if player already has max glitches equipped
        if (equippedGlitches.get(playerUUID).size() >= MAX_EQUIPPED_GLITCHES) {
            return false;
        }
        
        // Equip the glitch
        equippedGlitches.get(playerUUID).add(glitch);
        return true;
    }
    
    /**
     * Unequips a glitch for a player
     * 
     * @param player The player to unequip the glitch for
     * @param glitch The glitch to unequip
     * @return true if the glitch was unequipped, false if the player doesn't have the glitch equipped
     */
    public boolean unequipGlitch(Player player, Glitch glitch) {
        UUID playerUUID = player.getUniqueId();
        
        if (!equippedGlitches.containsKey(playerUUID)) {
            return false;
        }
        
        // If the glitch is active, deactivate it
        if (isGlitchActive(player, glitch)) {
            deactivateGlitch(player, glitch);
        }
        
        return equippedGlitches.get(playerUUID).remove(glitch);
    }
    
    /**
     * Activates a glitch for a player
     * 
     * @param player The player to activate the glitch for
     * @param glitch The glitch to activate
     * @return true if the glitch was activated, false if the player doesn't have the glitch equipped or it's on cooldown
     */
    public boolean activateGlitch(Player player, Glitch glitch) {
        UUID playerUUID = player.getUniqueId();
        
        // Check if player has the glitch equipped
        if (!isGlitchEquipped(player, glitch)) {
            return false;
        }
        
        // Try to activate the glitch
        if (!glitch.activate(player)) {
            return false;
        }
        
        // If the glitch has a duration, schedule its deactivation
        if (glitch.getDurationMillis() > 0) {
            // Initialize the map for this player if it doesn't exist
            activeGlitchTasks.computeIfAbsent(playerUUID, k -> new HashMap<>());
            
            // Schedule the deactivation task
            int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(
                plugin,
                () -> deactivateGlitch(player, glitch),
                glitch.getDurationMillis() / 50 // Convert milliseconds to ticks (1 tick = 50ms)
            );
            
            // Store the task ID
            activeGlitchTasks.get(playerUUID).put(glitch.getId(), taskId);
        }
        
        return true;
    }
    
    /**
     * Deactivates a glitch for a player
     * 
     * @param player The player to deactivate the glitch for
     * @param glitch The glitch to deactivate
     */
    public void deactivateGlitch(Player player, Glitch glitch) {
        UUID playerUUID = player.getUniqueId();
        
        // Deactivate the glitch
        glitch.deactivate(player);
        
        // Remove the task if it exists
        if (activeGlitchTasks.containsKey(playerUUID) && activeGlitchTasks.get(playerUUID).containsKey(glitch.getId())) {
            plugin.getServer().getScheduler().cancelTask(activeGlitchTasks.get(playerUUID).get(glitch.getId()));
            activeGlitchTasks.get(playerUUID).remove(glitch.getId());
        }
    }
    
    /**
     * Checks if a player has a glitch equipped
     * 
     * @param player The player to check
     * @param glitch The glitch to check for
     * @return true if the player has the glitch equipped, false otherwise
     */
    public boolean isGlitchEquipped(Player player, Glitch glitch) {
        UUID playerUUID = player.getUniqueId();
        
        return equippedGlitches.containsKey(playerUUID) && equippedGlitches.get(playerUUID).contains(glitch);
    }
    
    /**
     * Checks if a glitch is currently active for a player
     * 
     * @param player The player to check
     * @param glitch The glitch to check
     * @return true if the glitch is active, false otherwise
     */
    public boolean isGlitchActive(Player player, Glitch glitch) {
        UUID playerUUID = player.getUniqueId();
        
        return activeGlitchTasks.containsKey(playerUUID) && 
               activeGlitchTasks.get(playerUUID).containsKey(glitch.getId()) &&
               glitch.isActive();
    }
    
    /**
     * Gets all glitches owned by a player
     * 
     * @param player The player to get glitches for
     * @return A set of glitches owned by the player
     */
    public Set<Glitch> getOwnedGlitches(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        if (!ownedGlitches.containsKey(playerUUID)) {
            return Collections.emptySet();
        }
        
        return Collections.unmodifiableSet(ownedGlitches.get(playerUUID));
    }
    
    /**
     * Gets all glitches equipped by a player
     * 
     * @param player The player to get glitches for
     * @return A list of glitches equipped by the player
     */
    public List<Glitch> getEquippedGlitches(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        if (!equippedGlitches.containsKey(playerUUID)) {
            return Collections.emptyList();
        }
        
        return Collections.unmodifiableList(equippedGlitches.get(playerUUID));
    }
    
    /**
     * Cleans up all glitch data for a player (used when they leave the server)
     * 
     * @param player The player to clean up data for
     */
    public void cleanupPlayerData(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        // Deactivate any active glitches
        if (equippedGlitches.containsKey(playerUUID)) {
            for (Glitch glitch : equippedGlitches.get(playerUUID)) {
                if (isGlitchActive(player, glitch)) {
                    deactivateGlitch(player, glitch);
                }
            }
        }
        
        // Remove all task data
        activeGlitchTasks.remove(playerUUID);
        
        // We don't remove equipped or owned glitches here as they should persist
        // between sessions. This would be handled by a data storage system.
    }
}