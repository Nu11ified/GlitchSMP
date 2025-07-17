package org.nu11ified.glitchSMP;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.nu11ified.glitchSMP.command.GlitchCommand;
import org.nu11ified.glitchSMP.display.GlitchDisplay;
import org.nu11ified.glitchSMP.glitch.GlitchFactory;
import org.nu11ified.glitchSMP.manager.GlitchManager;

/**
 * Main plugin class for Glitch SMP.
 */
public final class GlitchSMP extends JavaPlugin implements Listener {
    private GlitchManager glitchManager;
    private GlitchFactory glitchFactory;
    private GlitchDisplay glitchDisplay;

    @Override
    public void onEnable() {
        // Initialize components
        glitchFactory = new GlitchFactory(this);
        glitchManager = new GlitchManager(this);
        glitchDisplay = new GlitchDisplay(this, glitchManager);
        
        // Register command
        GlitchCommand glitchCommand = new GlitchCommand(this, glitchManager, glitchFactory);
        getCommand("glitch").setExecutor(glitchCommand);
        getCommand("glitch").setTabCompleter(glitchCommand);
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);
        
        // Start displaying glitches for all online players
        glitchDisplay.startDisplayingForAll();
        
        // Log startup
        getLogger().info("Glitch SMP has been enabled!");
    }

    @Override
    public void onDisable() {
        // Stop displaying glitches for all online players
        if (glitchDisplay != null) {
            glitchDisplay.stopDisplayingForAll();
        }
        
        // Clean up player data
        if (glitchManager != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                glitchManager.cleanupPlayerData(player);
            }
        }
        
        // Log shutdown
        getLogger().info("Glitch SMP has been disabled!");
    }
    
    /**
     * Event handler for player join
     * Starts displaying glitches for the player
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        glitchDisplay.startDisplaying(player);
    }
    
    /**
     * Event handler for player quit
     * Stops displaying glitches for the player and cleans up their data
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        glitchDisplay.stopDisplaying(player);
        glitchManager.cleanupPlayerData(player);
    }
    
    /**
     * Gets the glitch manager instance
     * 
     * @return The glitch manager
     */
    public GlitchManager getGlitchManager() {
        return glitchManager;
    }
    
    /**
     * Gets the glitch factory instance
     * 
     * @return The glitch factory
     */
    public GlitchFactory getGlitchFactory() {
        return glitchFactory;
    }
    
    /**
     * Gets the glitch display instance
     * 
     * @return The glitch display
     */
    public GlitchDisplay getGlitchDisplay() {
        return glitchDisplay;
    }
}
