package org.nu11ified.glitchSMP.glitch;

/**
 * Enum of all available glitch types in the Glitch SMP plugin.
 */
public enum GlitchType {
    CRASH("Crash Glitch", "Crashes your opponent for 15s, their game reads 'CONNECTION THROTTLED' everytime they attempt to join Glitch SMP"),
    REDSTONE("Redstone Glitch", "Deactivates all redstone in the world for 30s, has a secret buff nobody knows about currently except for the current owner"),
    DREAM("Dream Glitch", "Disguises you as Dream, you have a higher tickrate in mob loot with better luck in their drops"),
    DUPE("Dupe Glitch", "Any item in your hand is duped / doubled upon activation. This excludes items such as the dragon egg, glitches, and shulkers."),
    INVENTORY("Inventory Glitch", "Completely rearranges your inventory, scrambles it messing up your opponents hotbar, no cooldown because it's very sudden"),
    ITEM("Item Glitch", "All weapons are deactivated and are turned null (useless). An example is when you're mid fight and the item glitch is activated, you can no longer use your sword for about 30s."),
    HEROBRINE("Herobrine Glitch", "Turn your skin into herobrine, name tag disguised and you have constant speed II. Whenever you take damage there's a chance you summon lightning on all nearby entities"),
    VIRUS("Virus Glitch", "Covers your screen completely with a virus or green mirage, the only thing visible is your inventory, your hearts and saturation bars are missing when it's activated. It affects any nearby players within a 6 block radius upon activation"),
    FAKE_BLOCK("Fake Block Glitch", "When activated, the block in your hand will be placed on your player's lower half. Basically creating a fake block that you can walk through, perfect for traps and messing with players"),
    FREEZE("Freeze Glitch", "Freezes a player in place for 30s, they cant use any items in their inventory or pearl away, pure fear as they get crit out losing hearts and barely making it out (most don't). Some say it's the most powerful glitch…"),
    EFFECT("Effect Glitch", "Amplifies your effects, only t1 pot effects are allowed but this will make it twice as stronger (Ex. Strength 1 -> Strength 2). The timer also stays the same so hypothetically you could have an 8 minute strength 2, it doesn't last for long but it's strong and useful"),
    IMMUNITY("Immunity Glitch", "Makes you immune to all damage for 30s"),
    TELEPORT("Teleport Glitch", "Teleports you to the block you look at from 20 blocks max (even air)"),
    GLIDE("Glide Glitch", "Flying in combat isn't allowed but this glitch token throws you into the sky and allows you to glide away from a fight. Very helpful for those who run out of fireworks. There's no limit to how long the glide lasts, just a long cooldown."),
    INVISIBILITY("Invisibility Glitch", "Turns you completely invisible, armour is also invisible along with any item you hold, lasts 30 seconds"),
    DIFFUSER("Diffuser Glitch", "Diffuses all glitches for 30s"),
    MORPH("Morph Glitch", "Morphs into a selected player, copying their armour trims and skin");

    private final String name;
    private final String description;

    /**
     * Constructor for GlitchType
     * 
     * @param name The display name of the glitch
     * @param description The description of the glitch
     */
    GlitchType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Gets the display name of the glitch
     * 
     * @return The display name
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
}