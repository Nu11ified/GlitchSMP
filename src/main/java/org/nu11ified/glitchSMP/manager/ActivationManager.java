package org.nu11ified.glitchSMP.manager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.nu11ified.glitchSMP.GlitchSMP;
import org.nu11ified.glitchSMP.glitch.Glitch;
import org.nu11ified.glitchSMP.glitch.GlitchType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the intuitive activation of glitches through player actions.
 * Handles offhand keybind and crouch detection for activating equipped glitches.
 */
public class ActivationManager implements Listener {
    private final GlitchSMP plugin;
    private final GlitchManager glitchManager;
    
    // Track which glitch slot each player is currently using
    private final Map<UUID, Integer> currentGlitchSlot = new HashMap<>();
    
    // Track sneaking state for each player
    private final Map<UUID, Boolean> playerSneaking = new HashMap<>();
    
    /**
     * Constructor for ActivationManager
     * 
     * @param plugin The main plugin instance
     * @param glitchManager The glitch manager instance
     */
    public ActivationManager(GlitchSMP plugin, GlitchManager glitchManager) {
        this.plugin = plugin;
        this.glitchManager = glitchManager;
    }
    
    /**
     * Handles player interaction events (right-clicking with glitch items)
     * 
     * @param event The player interact event
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Check if the player is right-clicking with a glitch item
        if (item != null && isGlitchItem(item)) {
            event.setCancelled(true); // Prevent default item usage
            
            // Get the glitch type from the item
            GlitchType glitchType = getGlitchTypeFromItem(item);
            if (glitchType != null) {
                // Check if player already owns this glitch
                boolean alreadyOwned = false;
                for (Glitch ownedGlitch : glitchManager.getOwnedGlitches(player)) {
                    if (ownedGlitch.getName().equals(glitchType.getName())) {
                        alreadyOwned = true;
                        break;
                    }
                }
                
                if (!alreadyOwned) {
                    // Give the glitch to the player
                    Glitch glitch = plugin.getGlitchFactory().createGlitch(glitchType);
                    boolean success = glitchManager.giveGlitch(player, glitch);
                    
                    if (success) {
                        player.sendMessage(ChatColor.GREEN + "You received " + glitch.getName() + "!");
                        player.sendMessage(ChatColor.YELLOW + "Use /glitch equip " + glitchType.getName().replace(" Glitch", "") + " to equip it.");
                        
                        // Remove the glitch item from inventory
                        if (item.getAmount() > 1) {
                            item.setAmount(item.getAmount() - 1);
                        } else {
                            player.getInventory().removeItem(item);
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You already own " + glitch.getName());
                    }
                } else {
                    player.sendMessage(ChatColor.YELLOW + "You already own " + glitchType.getName());
                }
            }
        }
    }
    
    /**
     * Handles offhand keybind events for glitch activation
     * 
     * @param event The player swap hand items event
     */
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Cancel the default offhand swap behavior
        event.setCancelled(true);
        
        // Get the player's equipped glitches
        List<Glitch> equippedGlitches = glitchManager.getEquippedGlitches(player);
        
        if (equippedGlitches.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You don't have any glitches equipped!");
            player.sendMessage(ChatColor.YELLOW + "Use /glitch equip <glitch> to equip a glitch.");
            return;
        }
        
        // Determine which glitch to activate based on sneaking state
        boolean isSneaking = playerSneaking.getOrDefault(playerUUID, false);
        int glitchIndex = isSneaking ? 1 : 0; // Left slot (1) if sneaking, right slot (0) if not
        
        // Ensure the glitch index is valid
        if (glitchIndex >= equippedGlitches.size()) {
            player.sendMessage(ChatColor.RED + "No glitch equipped in " + (isSneaking ? "left" : "right") + " slot!");
            return;
        }
        
        // Get the glitch to activate
        Glitch glitchToActivate = equippedGlitches.get(glitchIndex);
        
        // Try to activate the glitch
        boolean success = glitchManager.activateGlitch(player, glitchToActivate);
        
        if (success) {
            player.sendMessage(ChatColor.GREEN + "Activated " + glitchToActivate.getName() + "!");
            
            // Show which slot was used
            String slotName = isSneaking ? "left" : "right";
            player.sendMessage(ChatColor.GRAY + "Used " + slotName + " glitch slot");
            
            // Update the current glitch slot
            currentGlitchSlot.put(playerUUID, glitchIndex);
        } else {
            if (glitchToActivate.isOnCooldown()) {
                long cooldownSeconds = glitchToActivate.getRemainingCooldown() / 1000;
                player.sendMessage(ChatColor.RED + glitchToActivate.getName() + " is on cooldown for " + cooldownSeconds + " more seconds!");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to activate " + glitchToActivate.getName());
            }
        }
    }
    
    /**
     * Handles player sneaking events to track crouch state
     * 
     * @param event The player toggle sneak event
     */
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Update the sneaking state
        playerSneaking.put(playerUUID, event.isSneaking());
        
        // Show which glitch slot will be used
        if (event.isSneaking()) {
            List<Glitch> equippedGlitches = glitchManager.getEquippedGlitches(player);
            if (equippedGlitches.size() > 1) {
                player.sendMessage(ChatColor.YELLOW + "Left glitch slot selected (use offhand keybind to activate)");
            }
        } else {
            List<Glitch> equippedGlitches = glitchManager.getEquippedGlitches(player);
            if (!equippedGlitches.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "Right glitch slot selected (use offhand keybind to activate)");
            }
        }
    }
    
    /**
     * Checks if an item is a glitch item
     * 
     * @param item The item to check
     * @return true if it's a glitch item, false otherwise
     */
    private boolean isGlitchItem(ItemStack item) {
        if (item.getType() != org.bukkit.Material.NETHER_STAR) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        String displayName = meta.getDisplayName();
        return displayName.contains("Glitch");
    }
    
    /**
     * Gets the glitch type from a glitch item
     * 
     * @param item The glitch item
     * @return The glitch type, or null if not found
     */
    private GlitchType getGlitchTypeFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return null;
        }
        
        String displayName = meta.getDisplayName();
        // Remove color codes and "Glitch" suffix
        String cleanName = ChatColor.stripColor(displayName).replace(" Glitch", "");
        
        // Try to find the matching glitch type
        for (GlitchType type : GlitchType.values()) {
            if (type.getName().equals(cleanName + " Glitch")) {
                return type;
            }
        }
        
        return null;
    }
    
    /**
     * Gets the current glitch slot for a player
     * 
     * @param player The player
     * @return The current glitch slot index (0 for right, 1 for left)
     */
    public int getCurrentGlitchSlot(Player player) {
        return currentGlitchSlot.getOrDefault(player.getUniqueId(), 0);
    }
    
    /**
     * Checks if a player is currently sneaking
     * 
     * @param player The player
     * @return true if sneaking, false otherwise
     */
    public boolean isPlayerSneaking(Player player) {
        return playerSneaking.getOrDefault(player.getUniqueId(), false);
    }
    
    /**
     * Cleans up player data when they leave
     * 
     * @param player The player
     */
    public void cleanupPlayerData(Player player) {
        UUID playerUUID = player.getUniqueId();
        currentGlitchSlot.remove(playerUUID);
        playerSneaking.remove(playerUUID);
    }
}
