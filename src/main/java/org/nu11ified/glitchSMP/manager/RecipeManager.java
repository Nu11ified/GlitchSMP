package org.nu11ified.glitchSMP.manager;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.nu11ified.glitchSMP.GlitchSMP;
import org.nu11ified.glitchSMP.glitch.GlitchType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages crafting recipes for glitches.
 * Loads recipes from recipes.yml and registers them with the server.
 */
public class RecipeManager {
    private final GlitchSMP plugin;
    private final Map<GlitchType, ShapedRecipe> registeredRecipes = new HashMap<>();
    
    /**
     * Constructor for RecipeManager
     * 
     * @param plugin The main plugin instance
     */
    public RecipeManager(GlitchSMP plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Loads and registers all glitch crafting recipes
     */
    public void loadRecipes() {
        // Ensure recipes.yml exists
        if (!ensureRecipesFile()) {
            plugin.getLogger().severe("Failed to create recipes.yml file!");
            return;
        }
        
        // Load recipes from file
        org.bukkit.configuration.file.YamlConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
            new File(plugin.getDataFolder(), "recipes.yml")
        );
        
        // Register recipes for each glitch type
        for (GlitchType glitchType : GlitchType.values()) {
            registerRecipe(glitchType, config);
        }
        
        plugin.getLogger().info("Registered " + registeredRecipes.size() + " glitch crafting recipes");
    }
    
    /**
     * Ensures the recipes.yml file exists, creating it if necessary
     * 
     * @return true if successful, false otherwise
     */
    private boolean ensureRecipesFile() {
        File recipesFile = new File(plugin.getDataFolder(), "recipes.yml");
        
        if (!recipesFile.exists()) {
            // Create the plugin data folder if it doesn't exist
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            // Copy the default recipes.yml from resources
            try (InputStream inputStream = plugin.getClass().getClassLoader().getResourceAsStream("recipes.yml")) {
                if (inputStream != null) {
                    Files.copy(inputStream, recipesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    return true;
                } else {
                    plugin.getLogger().severe("Could not find recipes.yml in plugin resources!");
                    return false;
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create recipes.yml file", e);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Registers a crafting recipe for a specific glitch type
     * 
     * @param glitchType The glitch type to register a recipe for
     * @param config The configuration containing recipe data
     */
    private void registerRecipe(GlitchType glitchType, org.bukkit.configuration.file.YamlConfiguration config) {
        String glitchName = glitchType.name();
        
        // Check if recipe exists in config
        if (!config.contains(glitchName)) {
            plugin.getLogger().warning("No recipe found for " + glitchName + " in recipes.yml");
            return;
        }
        
        try {
            // Create the glitch item
            ItemStack glitchItem = createGlitchItem(glitchType);
            
            // Create the recipe
            NamespacedKey recipeKey = new NamespacedKey(plugin, "glitch_" + glitchName.toLowerCase());
            ShapedRecipe recipe = new ShapedRecipe(recipeKey, glitchItem);
            
            // Get the recipe shape
            java.util.List<String> shape = config.getStringList(glitchName);
            if (shape.size() != 3) {
                plugin.getLogger().warning("Invalid recipe shape for " + glitchName + " in recipes.yml");
                return;
            }
            
            // Set the recipe shape
            recipe.shape(shape.toArray(new String[0]));
            
            // Get the items mapping from config
            org.bukkit.configuration.ConfigurationSection itemsSection = config.getConfigurationSection("items");
            if (itemsSection == null) {
                plugin.getLogger().warning("No items section found in recipes.yml");
                return;
            }
            
            // Set the ingredients using the items mapping
            for (String key : itemsSection.getKeys(false)) {
                String itemId = itemsSection.getString(key);
                if (itemId != null) {
                    try {
                        Material material = parseMaterialFromId(itemId);
                        if (material != null) {
                            recipe.setIngredient(key.charAt(0), material);
                        } else {
                            plugin.getLogger().warning("Could not parse material from " + itemId + " for key " + key);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Invalid material " + itemId + " for key " + key + " in " + glitchName + " recipe");
                    }
                }
            }
            
            // Register the recipe
            plugin.getServer().addRecipe(recipe);
            registeredRecipes.put(glitchType, recipe);
            
            plugin.getLogger().info("Registered crafting recipe for " + glitchType.getName());
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to register recipe for " + glitchName, e);
        }
    }
    
    /**
     * Parses a material from an item ID string
     * Supports both "minecraft:item_name" and "ITEM_NAME" formats
     * 
     * @param itemId The item ID string
     * @return The Material, or null if not found
     */
    private Material parseMaterialFromId(String itemId) {
        try {
            // Handle "minecraft:item_name" format
            if (itemId.contains(":")) {
                String materialName = itemId.split(":")[1].toUpperCase();
                return Material.valueOf(materialName);
            } else {
                // Handle "ITEM_NAME" format
                return Material.valueOf(itemId.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material name: " + itemId);
            return null;
        }
    }
    
    /**
     * Creates a glitch item for the given glitch type
     * 
     * @param glitchType The glitch type
     * @return The glitch item
     */
    private ItemStack createGlitchItem(GlitchType glitchType) {
        // Use a custom item (e.g., NETHER_STAR) to represent glitches
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + glitchType.getName());
            
            // Add lore
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(ChatColor.GRAY + glitchType.getDescription());
            lore.add("");
            lore.add(ChatColor.YELLOW + "Right-click to equip this glitch");
            lore.add(ChatColor.YELLOW + "Use /glitch list to see your glitches");
            
            meta.setLore(lore);
            
            // Make it unbreakable and add custom model data
            meta.setUnbreakable(true);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Unregisters all registered recipes
     */
    public void unregisterRecipes() {
        for (ShapedRecipe recipe : registeredRecipes.values()) {
            plugin.getServer().removeRecipe(recipe.getKey());
        }
        registeredRecipes.clear();
        plugin.getLogger().info("Unregistered all glitch crafting recipes");
    }
    
    /**
     * Gets the registered recipe for a glitch type
     * 
     * @param glitchType The glitch type
     * @return The registered recipe, or null if not found
     */
    public ShapedRecipe getRecipe(GlitchType glitchType) {
        return registeredRecipes.get(glitchType);
    }
    
    /**
     * Checks if a recipe is registered for a glitch type
     * 
     * @param glitchType The glitch type
     * @return true if a recipe is registered, false otherwise
     */
    public boolean hasRecipe(GlitchType glitchType) {
        return registeredRecipes.containsKey(glitchType);
    }
}
