package org.nu11ified.glitchSMP.glitch.impl;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.nu11ified.glitchSMP.GlitchSMP;
import org.nu11ified.glitchSMP.glitch.Glitch;
import org.nu11ified.glitchSMP.glitch.GlitchType;

/**
 * Teleport Glitch implementation.
 * Teleports the player to the block they are looking at, up to 20 blocks away.
 */
public class TeleportGlitch extends Glitch {
    private static final long DURATION_MILLIS = 0; // Instant effect
    private static final long COOLDOWN_MILLIS = 30 * 1000; // 30 seconds
    private static final int MAX_DISTANCE = 20; // Maximum teleport distance
    
    /**
     * Constructor for TeleportGlitch
     * 
     * @param plugin The main plugin instance (not used)
     */
    public TeleportGlitch(GlitchSMP plugin) {
        super(
            GlitchType.TELEPORT.getName(),
            GlitchType.TELEPORT.getDescription(),
            COOLDOWN_MILLIS,
            DURATION_MILLIS
        );
    }
    
    @Override
    protected void onActivate(Player player) {
        // Get the block the player is looking at
        Block targetBlock = getTargetBlock(player, MAX_DISTANCE);
        
        if (targetBlock == null) {
            player.sendMessage("§cNo valid teleport location found within range.");
            return;
        }
        
        // Create particles at the current location
        player.getWorld().spawnParticle(
            Particle.PORTAL,
            player.getLocation(),
            50, // Amount
            0.5, 0.5, 0.5, // Offset
            1 // Speed
        );
        
        // Play sound at the current location
        player.getWorld().playSound(
            player.getLocation(),
            Sound.ENTITY_ENDERMAN_TELEPORT,
            1.0f, // Volume
            1.0f // Pitch
        );
        
        // Get the safe teleport location (on top of the block)
        Location teleportLocation = targetBlock.getLocation().add(0.5, 1, 0.5);
        teleportLocation.setYaw(player.getLocation().getYaw());
        teleportLocation.setPitch(player.getLocation().getPitch());
        
        // Teleport the player
        player.teleport(teleportLocation);
        
        // Create particles at the new location
        player.getWorld().spawnParticle(
            Particle.PORTAL,
            teleportLocation,
            50, // Amount
            0.5, 0.5, 0.5, // Offset
            1 // Speed
        );
        
        // Play sound at the new location
        player.getWorld().playSound(
            teleportLocation,
            Sound.ENTITY_ENDERMAN_TELEPORT,
            1.0f, // Volume
            1.0f // Pitch
        );
        
        // Send message to player
        player.sendMessage("§aYou activated the Teleport Glitch!");
    }
    
    @Override
    protected void onDeactivate(Player player) {
        // No deactivation needed for instant effects
    }
    
    /**
     * Gets the block the player is looking at
     * 
     * @param player The player
     * @param maxDistance The maximum distance to check
     * @return The target block, or null if none found
     */
    private Block getTargetBlock(Player player, int maxDistance) {
        BlockIterator iterator = new BlockIterator(player, maxDistance);
        Block block;
        
        while (iterator.hasNext()) {
            block = iterator.next();
            
            // Check if the block is air (we want to teleport to a solid block)
            if (!block.getType().isAir()) {
                return block;
            }
        }
        
        // If we're looking at air within range, get the last block
        if (iterator.hasNext()) {
            return iterator.next();
        }
        
        return null;
    }
}