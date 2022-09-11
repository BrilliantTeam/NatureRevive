package engineer.skyouo.plugins.naturerevive.constants;

import org.bukkit.Material;

public enum OreBlocks {
    COAL(Material.COAL_ORE),
    COPPER(Material.COPPER_ORE),
    IRON(Material.IRON_ORE),
    GOLD(Material.GOLD_ORE),
    DIAMOND(Material.DIAMOND_ORE),
    EMERALD(Material.EMERALD_ORE),
    LAPIS(Material.LAPIS_ORE),
    REDSTONE(Material.REDSTONE_ORE),

    DEEPSLATE_COAL(Material.DEEPSLATE_COAL_ORE),
    DEEPSLATE_COPPER(Material.DEEPSLATE_COPPER_ORE),
    DEEPSLATE_IRON(Material.DEEPSLATE_IRON_ORE),
    DEEPSLATE_GOLD(Material.DEEPSLATE_GOLD_ORE),
    DEEPSLATE_DIAMOND(Material.DEEPSLATE_DIAMOND_ORE),
    DEEPSLATE_EMERALD(Material.DEEPSLATE_EMERALD_ORE),
    DEEPSLATE_LAPIS(Material.DEEPSLATE_LAPIS_ORE),
    DEEPSLATE_REDSTONE(Material.DEEPSLATE_REDSTONE_ORE),

    NETHER_GOLD_ORE(Material.NETHER_GOLD_ORE),
    NETHER_QUARTZ_ORE(Material.NETHER_QUARTZ_ORE),
    ANCIANT_DEBRIS(Material.ANCIENT_DEBRIS);
    
    Material type;
    OreBlocks(Material material) {
        type = material;
    }

    public Material getType() {
        return type;
    }
    
    public static boolean contains(Material material) {
        for (OreBlocks oreBlocks: OreBlocks.values()) {
            if (oreBlocks.getType().equals(material)) {
                return true;
            }
        }

        return false;
    }
}
