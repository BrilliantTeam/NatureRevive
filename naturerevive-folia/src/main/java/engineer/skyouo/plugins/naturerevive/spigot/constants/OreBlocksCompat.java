package engineer.skyouo.plugins.naturerevive.spigot.constants;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class OreBlocksCompat {
    private static List<Material> oreBlocks = new ArrayList<>();

    public static void addMaterial(Material material) {
        oreBlocks.add(material);
    }

    public static Material getSpecialMaterial(String name) {
        return Material.getMaterial(name) != null ? Material.getMaterial(name) : null;
    }

    public static List<Material> values() {
        return oreBlocks;
    }

    public static boolean contains(Material material) {
        return oreBlocks.contains(material);
    }
}
