package org.nu11ified.glitchSMP.glitch.impl;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.nu11ified.glitchSMP.GlitchSMP;
import org.nu11ified.glitchSMP.glitch.Glitch;
import org.nu11ified.glitchSMP.glitch.GlitchType;

/**
 * Invisibility Glitch implementation.
 * Turns the player completely invisible, including armor and held items, for 30 seconds.
 */
public class InvisibilityGlitch extends Glitch {
    private static final long DURATION_MILLIS = 30 * 1000; // 30 seconds
    private static final long COOLDOWN_MILLIS = 3 * 60 * 1000; // 3 minutes
    
    /**
     * Constructor for InvisibilityGlitch
     */
    public InvisibilityGlitch() {
        super(
            GlitchType.INVISIBILITY.getName(),
            GlitchType.INVISIBILITY.getDescription(),
            COOLDOWN_MILLIS,
            DURATION_MILLIS
        );
    }
    
    @Override
    protected void onActivate(Player player) {
        // Apply invisibility effect with maximum level (255) to hide armor and held items
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.INVISIBILITY,
            (int) (DURATION_MILLIS / 50), // Convert milliseconds to ticks (1 tick = 50ms)
            255, // Maximum level to hide armor and held items
            false, // Don't show particles
            false, // Don't show icon
            true // Show particles
        ));
        
        // Send message to player
        player.sendMessage("§aYou activated the Invisibility Glitch! You are now completely invisible for 30 seconds.");
    }
    
    @Override
    protected void onDeactivate(Player player) {
        // Remove invisibility effect
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        
        // Send message to player
        player.sendMessage("§cYour Invisibility Glitch has worn off.");
    }
}