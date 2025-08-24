package org.nu11ified.glitchSMP.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.nu11ified.glitchSMP.GlitchSMP;
import org.nu11ified.glitchSMP.glitch.Glitch;
import org.nu11ified.glitchSMP.glitch.GlitchFactory;
import org.nu11ified.glitchSMP.glitch.GlitchType;
import org.nu11ified.glitchSMP.manager.GlitchManager;
import org.nu11ified.glitchSMP.manager.CraftingLimiter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command executor for the glitch command.
 */
public class GlitchCommand implements CommandExecutor, TabCompleter {
    private final GlitchManager glitchManager;
    private final GlitchFactory glitchFactory;
    private final CraftingLimiter craftingLimiter;
    
    /**
     * Constructor for GlitchCommand
     * 
     * @param plugin The main plugin instance (not used)
     * @param glitchManager The glitch manager instance
     * @param glitchFactory The glitch factory instance
     * @param craftingLimiter The crafting limiter instance
     */
    public GlitchCommand(GlitchSMP plugin, GlitchManager glitchManager, GlitchFactory glitchFactory, CraftingLimiter craftingLimiter) {
        this.glitchManager = glitchManager;
        this.glitchFactory = glitchFactory;
        this.craftingLimiter = craftingLimiter;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "give":
                return handleGiveCommand(sender, args);
            case "equip":
                return handleEquipCommand(sender, args);
            case "unequip":
                return handleUnequipCommand(sender, args);
            case "list":
                return handleListCommand(sender, args);
            case "reset":
                return handleResetCommand(sender, args);
            case "status":
                return handleStatusCommand(sender, args);
            case "help":
                sendHelpMessage(sender);
                return true;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                sendHelpMessage(sender);
                return true;
        }
    }
    
    /**
     * Handles the give subcommand
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return true if the command was handled, false otherwise
     */
    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        // Check permission
        if (!sender.hasPermission("glitchsmp.command.glitch.give")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        // Check arguments
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /glitch give <player> <glitch>");
            return true;
        }
        
        // Get player
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return true;
        }
        
        // Get glitch type
        GlitchType glitchType;
        try {
            glitchType = GlitchType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Unknown glitch type: " + args[2]);
            return true;
        }
        
        // Create and give glitch
        Glitch glitch = glitchFactory.createGlitch(glitchType);
        boolean success = glitchManager.giveGlitch(target, glitch);
        
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Gave " + glitch.getName() + " to " + target.getName());
            target.sendMessage(ChatColor.GREEN + "You received " + glitch.getName());
        } else {
            sender.sendMessage(ChatColor.RED + target.getName() + " already has " + glitch.getName());
        }
        
        return true;
    }
    
    /**
     * Handles the equip subcommand
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return true if the command was handled, false otherwise
     */
    private boolean handleEquipCommand(CommandSender sender, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        // Check permission
        if (!sender.hasPermission("glitchsmp.command.glitch.equip")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        // Check arguments
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /glitch equip <glitch>");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Get glitch type
        GlitchType glitchType;
        try {
            glitchType = GlitchType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Unknown glitch type: " + args[1]);
            return true;
        }
        
        // Find the glitch in the player's owned glitches
        Glitch glitchToEquip = null;
        for (Glitch ownedGlitch : glitchManager.getOwnedGlitches(player)) {
            if (ownedGlitch.getName().equals(glitchType.getName())) {
                glitchToEquip = ownedGlitch;
                break;
            }
        }
        
        if (glitchToEquip == null) {
            sender.sendMessage(ChatColor.RED + "You don't own " + glitchType.getName());
            return true;
        }
        
        // Equip the glitch
        boolean success = glitchManager.equipGlitch(player, glitchToEquip);
        
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Equipped " + glitchToEquip.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to equip " + glitchToEquip.getName() + ". You may already have the maximum number of glitches equipped.");
        }
        
        return true;
    }
    
    /**
     * Handles the unequip subcommand
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return true if the command was handled, false otherwise
     */
    private boolean handleUnequipCommand(CommandSender sender, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        // Check permission
        if (!sender.hasPermission("glitchsmp.command.glitch.unequip")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        // Check arguments
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /glitch unequip <glitch>");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Get glitch type
        GlitchType glitchType;
        try {
            glitchType = GlitchType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Unknown glitch type: " + args[1]);
            return true;
        }
        
        // Find the glitch in the player's equipped glitches
        Glitch glitchToUnequip = null;
        for (Glitch equippedGlitch : glitchManager.getEquippedGlitches(player)) {
            if (equippedGlitch.getName().equals(glitchType.getName())) {
                glitchToUnequip = equippedGlitch;
                break;
            }
        }
        
        if (glitchToUnequip == null) {
            sender.sendMessage(ChatColor.RED + "You don't have " + glitchType.getName() + " equipped");
            return true;
        }
        
        // Unequip the glitch
        boolean success = glitchManager.unequipGlitch(player, glitchToUnequip);
        
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Unequipped " + glitchToUnequip.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to unequip " + glitchToUnequip.getName());
        }
        
        return true;
    }
    
    /**
     * Handles the list subcommand
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return true if the command was handled, false otherwise
     */
    private boolean handleListCommand(CommandSender sender, String[] args) {
        // Check permission
        if (!sender.hasPermission("glitchsmp.command.glitch.list")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
            // List all available glitch types
            sender.sendMessage(ChatColor.YELLOW + "Available Glitch Types:");
            for (GlitchType type : GlitchType.values()) {
                sender.sendMessage(ChatColor.YELLOW + "- " + type.getName() + ": " + ChatColor.GRAY + type.getDescription());
            }
        } else if (sender instanceof Player) {
            // List player's owned and equipped glitches
            Player player = (Player) sender;
            
            sender.sendMessage(ChatColor.YELLOW + "Your Glitches:");
            
            // Equipped glitches
            List<Glitch> equippedGlitches = glitchManager.getEquippedGlitches(player);
            sender.sendMessage(ChatColor.YELLOW + "Equipped (" + equippedGlitches.size() + "/2):");
            if (equippedGlitches.isEmpty()) {
                sender.sendMessage(ChatColor.GRAY + "  None");
            } else {
                for (Glitch glitch : equippedGlitches) {
                    sender.sendMessage(ChatColor.GREEN + "  - " + glitch.getName());
                }
            }
            
            // Owned glitches
            sender.sendMessage(ChatColor.YELLOW + "Owned:");
            List<Glitch> ownedGlitches = new ArrayList<>(glitchManager.getOwnedGlitches(player));
            if (ownedGlitches.isEmpty()) {
                sender.sendMessage(ChatColor.GRAY + "  None");
            } else {
                for (Glitch glitch : ownedGlitches) {
                    boolean isEquipped = equippedGlitches.contains(glitch);
                    
                    // Show glitch name with color based on equipped status
                    sender.sendMessage((isEquipped ? ChatColor.GREEN : ChatColor.GRAY) + "  - " + glitch.getName());
                    
                    // Show glitch description
                    sender.sendMessage(ChatColor.GRAY + "    " + glitch.getDescription());
                    
                    // Show cooldown information
                    long cooldownSeconds = glitch.getCooldownMillis() / 1000;
                    sender.sendMessage(ChatColor.GRAY + "    Cooldown: " + cooldownSeconds + " seconds");
                    
                    // Add a blank line for readability
                    sender.sendMessage("");
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /glitch list [all]");
        }
        
        return true;
    }
    
    /**
     * Handles the reset subcommand (admin only)
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return true if the command was handled, false otherwise
     */
    private boolean handleResetCommand(CommandSender sender, String[] args) {
        // Check permission
        if (!sender.hasPermission("glitchsmp.command.glitch.reset")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        // Check arguments
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /glitch reset <player>");
            return true;
        }
        
        // Get player
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return true;
        }
        
        // Reset the player's crafted glitch count
        craftingLimiter.resetCraftedGlitchCount(target);
        sender.sendMessage(ChatColor.GREEN + "Reset " + target.getName() + "'s glitch crafting count.");
        
        return true;
    }
    
    /**
     * Handles the status subcommand (admin only)
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return true if the command was handled, false otherwise
     */
    private boolean handleStatusCommand(CommandSender sender, String[] args) {
        // Check permission
        if (!sender.hasPermission("glitchsmp.command.glitch.status")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        // Check arguments
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /glitch status <player>");
            return true;
        }
        
        // Get player
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return true;
        }
        
        // Get player's glitch status
        int craftedCount = craftingLimiter.getPlayerCraftedGlitchCount(target);
        java.util.Set<Glitch> ownedGlitches = glitchManager.getOwnedGlitches(target);
        List<Glitch> equippedGlitches = glitchManager.getEquippedGlitches(target);
        
        sender.sendMessage(ChatColor.YELLOW + "=== " + target.getName() + "'s Glitch Status ===");
        sender.sendMessage(ChatColor.GRAY + "Crafted Glitches: " + ChatColor.WHITE + craftedCount + "/2");
        sender.sendMessage(ChatColor.GRAY + "Owned Glitches: " + ChatColor.WHITE + ownedGlitches.size());
        sender.sendMessage(ChatColor.GRAY + "Equipped Glitches: " + ChatColor.WHITE + equippedGlitches.size());
        
        if (!ownedGlitches.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Owned:");
            for (Glitch glitch : ownedGlitches) {
                boolean isEquipped = equippedGlitches.contains(glitch);
                ChatColor color = isEquipped ? ChatColor.GREEN : ChatColor.WHITE;
                sender.sendMessage(color + "  - " + glitch.getName());
            }
        }
        
        return true;
    }
    
    /**
     * Sends the help message to the sender
     * 
     * @param sender The command sender
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Glitch SMP Commands:");
        sender.sendMessage(ChatColor.YELLOW + "/glitch give <player> <glitch> " + ChatColor.GRAY + "- Gives a glitch to a player");
        sender.sendMessage(ChatColor.YELLOW + "/glitch equip <glitch> " + ChatColor.GRAY + "- Equips a glitch");
        sender.sendMessage(ChatColor.YELLOW + "/glitch unequip <glitch> " + ChatColor.GRAY + "- Unequips a glitch");
        sender.sendMessage(ChatColor.YELLOW + "/glitch list [all] " + ChatColor.GRAY + "- Lists your glitches or all available glitches");
        sender.sendMessage(ChatColor.YELLOW + "/glitch help " + ChatColor.GRAY + "- Shows this help message");
        
        // Admin commands
        if (sender.hasPermission("glitchsmp.command.glitch.reset")) {
            sender.sendMessage(ChatColor.YELLOW + "/glitch reset <player> " + ChatColor.GRAY + "- Resets player's glitch crafting count");
        }
        if (sender.hasPermission("glitchsmp.command.glitch.status")) {
            sender.sendMessage(ChatColor.YELLOW + "/glitch status <player> " + ChatColor.GRAY + "- Shows player's glitch status");
        }
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Glitch Activation:");
        sender.sendMessage(ChatColor.GRAY + "• Right-click glitch items to obtain them");
        sender.sendMessage(ChatColor.GRAY + "• Use offhand keybind (F) to activate right slot glitch");
        sender.sendMessage(ChatColor.GRAY + "• Crouch + offhand keybind to activate left slot glitch");
        sender.sendMessage(ChatColor.GRAY + "• Craft glitches using recipes in recipes.yml");
        sender.sendMessage(ChatColor.GRAY + "• Limited to 2 glitches per player (drops on death)");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Suggest subcommands
            List<String> subcommands = new ArrayList<>();
            subcommands.addAll(Arrays.asList("give", "equip", "unequip", "list", "help"));
            
            // Add admin commands if player has permission
            if (sender.hasPermission("glitchsmp.command.glitch.reset")) {
                subcommands.add("reset");
            }
            if (sender.hasPermission("glitchsmp.command.glitch.status")) {
                subcommands.add("status");
            }
            
            return subcommands.stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Suggest based on subcommand
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("give")) {
                // Suggest online players
                return Bukkit.getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            } else if (subCommand.equals("equip")) {
                // Suggest owned glitches
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    return glitchManager.getOwnedGlitches(player)
                        .stream()
                        .map(g -> g.getName().replace(" Glitch", "").toUpperCase())
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
            } else if (subCommand.equals("unequip")) {
                // Suggest equipped glitches
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    return glitchManager.getEquippedGlitches(player)
                        .stream()
                        .map(g -> g.getName().replace(" Glitch", "").toUpperCase())
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
            } else if (subCommand.equals("list")) {
                // Suggest "all"
                return Arrays.asList("all")
                    .stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        } else if (args.length == 3 && args[0].toLowerCase().equals("give")) {
            // Suggest glitch types for give command
            return Arrays.stream(GlitchType.values())
                .map(GlitchType::name)
                .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}