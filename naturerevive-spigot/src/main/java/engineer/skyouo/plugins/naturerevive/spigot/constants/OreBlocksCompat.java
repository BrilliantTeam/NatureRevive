package engineer.skyouo.plugins.naturerevive.spigot.constants;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public class OreBlocksCompat {
    private static Set<Material> oreBlocks = new HashSet<>();

    public static void addMaterial(Material material) {
        oreBlocks.add(material);
    }

    public static Material getSpecialMaterial(String name) {
        return Material.getMaterial(name) != null ? Material.getMaterial(name) : null;
    }

    public static Set<Material> values() {
        return oreBlocks;
    }

    public static boolean contains(Material material) {
        return oreBlocks.contains(material);
    }
}
