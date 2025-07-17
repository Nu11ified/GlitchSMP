package org.nu11ified.glitchSMP.glitch;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Base class for all glitches in the Glitch SMP plugin.
 */
public abstract class Glitch {
    private final String name;
    private final String description;
    private final long cooldownMillis;
    private final long durationMillis;
    private final UUID id;
    
    // Track when this glitch was last activated
    private long lastActivationTime = 0;
    
    /**
     * Constructor for a glitch
     * 
     * @param name The name of the glitch
     * @param description The description of the glitch
     * @param cooldownMillis The cooldown time in milliseconds
     * @param durationMillis The duration time in milliseconds (0 for instant effects)
     */
    public Glitch(String name, String description, long cooldownMillis, long durationMillis) {
        this.name = name;
        this.description = description;
        this.cooldownMillis = cooldownMillis;
        this.durationMillis = durationMillis;
        this.id = UUID.randomUUID();
    }
    
    /**
     * Activates the glitch for the given player
     * 
     * @param player The player activating the glitch
     * @return true if activation was successful, false if on cooldown
     */
    public boolean activate(Player player) {
        if (isOnCooldown()) {
            return false;
        }
        
        // Set last activation time
        lastActivationTime = System.currentTimeMillis();
        
        // Execute the glitch effect
        onActivate(player);
        
        return true;
    }
    
    /**
     * Deactivates the glitch for the given player
     * 
     * @param player The player to deactivate the glitch for
     */
    public void deactivate(Player player) {
        onDeactivate(player);
    }
    
    /**
     * Checks if the glitch is currently on cooldown
     * 
     * @return true if on cooldown, false otherwise
     */
    public boolean isOnCooldown() {
        return System.currentTimeMillis() - lastActivationTime < cooldownMillis;
    }
    
    /**
     * Checks if the glitch is currently active
     * 
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return System.currentTimeMillis() - lastActivationTime < durationMillis;
    }
    
    /**
     * Gets the remaining cooldown time in milliseconds
     * 
     * @return The remaining cooldown time
     */
    public long getRemainingCooldown() {
        if (!isOnCooldown()) {
            return 0;
        }
        return cooldownMillis - (System.currentTimeMillis() - lastActivationTime);
    }
    
    /**
     * Gets the remaining duration time in milliseconds
     * 
     * @return The remaining duration time
     */
    public long getRemainingDuration() {
        if (!isActive()) {
            return 0;
        }
        return durationMillis - (System.currentTimeMillis() - lastActivationTime);
    }
    
    /**
     * Gets the name of the glitch
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the description of the glitch
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the cooldown time in milliseconds
     * 
     * @return The cooldown time
     */
    public long getCooldownMillis() {
        return cooldownMillis;
    }
    
    /**
     * Gets the duration time in milliseconds
     * 
     * @return The duration time
     */
    public long getDurationMillis() {
        return durationMillis;
    }
    
    /**
     * Gets the unique ID of this glitch instance
     * 
     * @return The UUID
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Implementation of the glitch activation effect
     * 
     * @param player The player activating the glitch
     */
    protected abstract void onActivate(Player player);
    
    /**
     * Implementation of the glitch deactivation effect
     * 
     * @param player The player deactivating the glitch
     */
    protected abstract void onDeactivate(Player player);
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Glitch glitch = (Glitch) obj;
        return id.equals(glitch.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}