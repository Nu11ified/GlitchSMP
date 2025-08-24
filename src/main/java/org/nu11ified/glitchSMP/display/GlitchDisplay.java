package org.nu11ified.glitchSMP.display;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.nu11ified.glitchSMP.GlitchSMP;
import org.nu11ified.glitchSMP.glitch.Glitch;
import org.nu11ified.glitchSMP.manager.GlitchManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles displaying equipped glitches above the player's hotbar.
 */
public class GlitchDisplay {
    private final GlitchSMP plugin;
    private final GlitchManager glitchManager;
    
    // Map of player UUIDs to their action bar display tasks
    private final ConcurrentHashMap<UUID, BukkitTask> displayTasks = new ConcurrentHashMap<>();
    
    // Update interval in ticks (1 second = 20 ticks)
    private static final long UPDATE_INTERVAL = 10;
    
    /**
     * Constructor for GlitchDisplay
     * 
     * @param plugin The main plugin instance
     * @param glitchManager The glitch manager instance
     */
    public GlitchDisplay(GlitchSMP plugin, GlitchManager glitchManager) {
        this.plugin = plugin;
        this.glitchManager = glitchManager;
    }
    
    /**
     * Starts displaying equipped glitches for a player
     * 
     * @param player The player to start displaying for
     */
    public void startDisplaying(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        // Cancel any existing task
        stopDisplaying(player);
        
        // Start a new task
        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            updateDisplay(player);
        }, 0, UPDATE_INTERVAL);
        
        // Store the task
        displayTasks.put(playerUUID, task);
    }
    
    /**
     * Stops displaying equipped glitches for a player
     * 
     * @param player The player to stop displaying for
     */
    public void stopDisplaying(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        // Cancel and remove any existing task
        if (displayTasks.containsKey(playerUUID)) {
            displayTasks.get(playerUUID).cancel();
            displayTasks.remove(playerUUID);
        }
    }
    
    /**
     * Updates the display for a player
     * 
     * @param player The player to update the display for
     */
    private void updateDisplay(Player player) {
        // Get the player's equipped glitches
        List<Glitch> equippedGlitches = glitchManager.getEquippedGlitches(player);
        
        // If the player has no equipped glitches, don't display anything
        if (equippedGlitches.isEmpty()) {
            return;
        }
        
        // Build the display string
        StringBuilder displayBuilder = new StringBuilder();
        
        // Add slot indicator
        displayBuilder.append(ChatColor.GOLD).append("Glitches: ");
        
        // Add each equipped glitch to the display
        for (int i = 0; i < equippedGlitches.size(); i++) {
            Glitch glitch = equippedGlitches.get(i);
            
            // Add a separator between glitches
            if (i > 0) {
                displayBuilder.append(" ").append(ChatColor.GRAY).append("|").append(" ");
            }
            
            // Add slot indicator
            String slotName = (i == 0) ? "R" : "L"; // Right (0) or Left (1)
            displayBuilder.append(ChatColor.AQUA).append("[").append(slotName).append("] ");
            
            // Add the glitch name with appropriate color
            if (glitchManager.isGlitchActive(player, glitch)) {
                // Active glitch - green
                displayBuilder.append(ChatColor.GREEN);
                displayBuilder.append(glitch.getName());
                
                // Add remaining duration for active glitches
                long durationSeconds = glitch.getRemainingDuration() / 1000;
                displayBuilder.append(" (").append(durationSeconds).append("s)");
            } else if (glitch.isOnCooldown()) {
                // Glitch on cooldown - red
                displayBuilder.append(ChatColor.RED);
                displayBuilder.append(glitch.getName());
                
                // Add cooldown time if on cooldown
                long cooldownSeconds = glitch.getRemainingCooldown() / 1000;
                displayBuilder.append(" (").append(cooldownSeconds).append("s)");
            } else {
                // Ready glitch - yellow
                displayBuilder.append(ChatColor.YELLOW);
                displayBuilder.append(glitch.getName());
                displayBuilder.append(" ").append(ChatColor.GREEN).append("✓");
            }
        }
        
        // Add activation hint
        if (equippedGlitches.size() > 0) {
            displayBuilder.append(" ").append(ChatColor.GRAY).append("| ");
            displayBuilder.append(ChatColor.WHITE).append("Offhand: Right, Crouch+Offhand: Left");
        }
        
        // Send the action bar message
        sendActionBar(player, displayBuilder.toString());
    }
    
    /**
     * Sends an action bar message to a player
     * 
     * @param player The player to send the message to
     * @param message The message to send
     */
    private void sendActionBar(Player player, String message) {
        // Use Paper's API to send action bar message
        player.sendActionBar(message);
    }
    
    /**
     * Starts displaying equipped glitches for all online players
     */
    public void startDisplayingForAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            startDisplaying(player);
        }
    }
    
    /**
     * Stops displaying equipped glitches for all online players
     */
    public void stopDisplayingForAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            stopDisplaying(player);
        }
    }
}