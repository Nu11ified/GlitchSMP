package org.nu11ified.glitchSMP.manager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.nu11ified.glitchSMP.GlitchSMP;
import org.nu11ified.glitchSMP.glitch.Glitch;
import org.nu11ified.glitchSMP.glitch.GlitchType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Manages crafting limitations and death mechanics for glitches.
 * Ensures players can only have 2 glitches and handles glitch dropping on death.
 */
public class CraftingLimiter implements Listener {
    private final GlitchSMP plugin;
    private final GlitchManager glitchManager;
    
    // Track how many glitches each player has crafted
    private final Map<UUID, Integer> craftedGlitchCount = new HashMap<>();
    
    // Random generator for selecting which glitch to drop
    private final Random random = new Random();
    
    /**
     * Constructor for CraftingLimiter
     * 
     * @param plugin The main plugin instance
     * @param glitchManager The glitch manager instance
     */
    public CraftingLimiter(GlitchSMP plugin, GlitchManager glitchManager) {
        this.plugin = plugin;
        this.glitchManager = glitchManager;
    }
    
    /**
     * Handles crafting events to limit glitch creation
     * 
     * @param event The craft item event
     */
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack result = event.getCurrentItem();
        
        // Check if the crafted item is a glitch
        if (isGlitchItem(result)) {
            // Check if player has reached the glitch limit
            if (hasReachedGlitchLimit(player)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can only craft 2 glitches! You must die to lose one before crafting another.");
                return;
            }
            
            // Increment the crafted glitch count
            incrementCraftedGlitchCount(player);
            player.sendMessage(ChatColor.GREEN + "Glitch crafted! You have " + getCraftedGlitchCount(player) + "/2 glitches.");
        }
    }
    
    /**
     * Handles player death to drop one random glitch
     * 
     * @param event The player death event
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        java.util.Set<Glitch> ownedGlitches = glitchManager.getOwnedGlitches(player);
        
        if (ownedGlitches.isEmpty()) {
            return;
        }
        
        // Convert Set to List for random selection
        java.util.List<Glitch> glitchesList = new java.util.ArrayList<>(ownedGlitches);
        
        // Select a random glitch to drop
        Glitch glitchToDrop = glitchesList.get(random.nextInt(glitchesList.size()));
        
        // Remove the glitch from the player
        glitchManager.removeGlitch(player, glitchToDrop);
        
        // Create a glitch item to drop
        ItemStack glitchItem = createGlitchItem(glitchToDrop);
        
        // Drop the glitch item at the death location
        Location deathLocation = player.getLocation();
        player.getWorld().dropItemNaturally(deathLocation, glitchItem);
        
        // Send message to the player
        player.sendMessage(ChatColor.RED + "You dropped " + glitchToDrop.getName() + " on death!");
        
        // Decrement the crafted glitch count
        decrementCraftedGlitchCount(player);
        
        // Send message about being able to craft again
        if (getCraftedGlitchCount(player) < 2) {
            player.sendMessage(ChatColor.YELLOW + "You can now craft " + (2 - getCraftedGlitchCount(player)) + " more glitch(es).");
        }
    }
    
    /**
     * Handles item pickup to prevent duplicate glitches
     * 
     * @param event The player pickup item event
     */
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();
        
        // Check if the picked up item is a glitch
        if (isGlitchItem(item)) {
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
                
                if (alreadyOwned) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You already own " + glitchType.getName() + "! You cannot pick up duplicate glitches.");
                    return;
                }
                
                // Check if player has reached the glitch limit
                if (hasReachedGlitchLimit(player)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You can only have 2 glitches! You must die to lose one before picking up another.");
                    return;
                }
                
                // Increment the crafted glitch count (since they're getting a glitch)
                incrementCraftedGlitchCount(player);
                player.sendMessage(ChatColor.GREEN + "You picked up " + glitchType.getName() + "! You now have " + getCraftedGlitchCount(player) + "/2 glitches.");
            }
        }
    }
    
    /**
     * Checks if a player has reached the glitch limit
     * 
     * @param player The player to check
     * @return true if the player has reached the limit, false otherwise
     */
    private boolean hasReachedGlitchLimit(Player player) {
        return getCraftedGlitchCount(player) >= 2;
    }
    
    /**
     * Gets the number of glitches a player has crafted
     * 
     * @param player The player
     * @return The number of crafted glitches
     */
    private int getCraftedGlitchCount(Player player) {
        return craftedGlitchCount.getOrDefault(player.getUniqueId(), 0);
    }
    
    /**
     * Increments the crafted glitch count for a player
     * 
     * @param player The player
     */
    private void incrementCraftedGlitchCount(Player player) {
        UUID playerUUID = player.getUniqueId();
        int currentCount = craftedGlitchCount.getOrDefault(playerUUID, 0);
        craftedGlitchCount.put(playerUUID, currentCount + 1);
    }
    
    /**
     * Decrements the crafted glitch count for a player
     * 
     * @param player The player
     */
    private void decrementCraftedGlitchCount(Player player) {
        UUID playerUUID = player.getUniqueId();
        int currentCount = craftedGlitchCount.getOrDefault(playerUUID, 0);
        if (currentCount > 0) {
            craftedGlitchCount.put(playerUUID, currentCount - 1);
        }
    }
    
    /**
     * Checks if an item is a glitch item
     * 
     * @param item The item to check
     * @return true if it's a glitch item, false otherwise
     */
    private boolean isGlitchItem(ItemStack item) {
        if (item.getType() != Material.NETHER_STAR) {
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
     * Creates a glitch item for the given glitch
     * 
     * @param glitch The glitch
     * @return The glitch item
     */
    private ItemStack createGlitchItem(Glitch glitch) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + glitch.getName());
            
            // Add lore
            List<String> lore = new java.util.ArrayList<>();
            lore.add(ChatColor.GRAY + glitch.getDescription());
            lore.add("");
            lore.add(ChatColor.YELLOW + "Right-click to equip this glitch");
            lore.add(ChatColor.YELLOW + "Use /glitch list to see your glitches");
            
            meta.setLore(lore);
            meta.setUnbreakable(true);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Cleans up player data when they leave
     * 
     * @param player The player
     */
    public void cleanupPlayerData(Player player) {
        // Note: We don't remove crafted glitch count on disconnect
        // This ensures the limit persists across sessions
    }
    
    /**
     * Gets the crafted glitch count for a player (for admin commands)
     * 
     * @param player The player
     * @return The number of crafted glitches
     */
    public int getPlayerCraftedGlitchCount(Player player) {
        return getCraftedGlitchCount(player);
    }
    
    /**
     * Resets the crafted glitch count for a player (admin command)
     * 
     * @param player The player
     */
    public void resetCraftedGlitchCount(Player player) {
        craftedGlitchCount.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Your glitch crafting count has been reset!");
    }
}
