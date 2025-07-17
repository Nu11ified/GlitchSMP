package org.nu11ified.glitchSMP.glitch.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.nu11ified.glitchSMP.GlitchSMP;
import org.nu11ified.glitchSMP.glitch.Glitch;
import org.nu11ified.glitchSMP.glitch.GlitchType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Immunity Glitch implementation.
 * Makes the player immune to all damage for 30 seconds.
 */
public class ImmunityGlitch extends Glitch implements Listener {
    private static final long DURATION_MILLIS = 30 * 1000; // 30 seconds
    private static final long COOLDOWN_MILLIS = 5 * 60 * 1000; // 5 minutes
    
    private final GlitchSMP plugin;
    private final Set<UUID> immunePlayers = new HashSet<>();
    
    /**
     * Constructor for ImmunityGlitch
     * 
     * @param plugin The main plugin instance
     */
    public ImmunityGlitch(GlitchSMP plugin) {
        super(
            GlitchType.IMMUNITY.getName(),
            GlitchType.IMMUNITY.getDescription(),
            COOLDOWN_MILLIS,
            DURATION_MILLIS
        );
        this.plugin = plugin;
    }
    
    @Override
    protected void onActivate(Player player) {
        // Register the event listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // Add player to immune set
        immunePlayers.add(player.getUniqueId());
        
        // Visual effect to show immunity
        player.setGlowing(true);
        
        // Send message to player
        player.sendMessage("§aYou activated the Immunity Glitch! You are now immune to all damage for 30 seconds.");
    }
    
    @Override
    protected void onDeactivate(Player player) {
        // Remove player from immune set
        immunePlayers.remove(player.getUniqueId());
        
        // Remove visual effect
        player.setGlowing(false);
        
        // If no more immune players, unregister the event listener
        if (immunePlayers.isEmpty()) {
            HandlerList.unregisterAll(this);
        }
        
        // Send message to player
        player.sendMessage("§cYour Immunity Glitch has worn off.");
    }
    
    /**
     * Event handler for entity damage
     * Cancels damage for immune players
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            if (immunePlayers.contains(player.getUniqueId())) {
                // Cancel the damage event
                event.setCancelled(true);
                
                // Visual feedback
                player.getWorld().strikeLightningEffect(player.getLocation());
            }
        }
    }
}