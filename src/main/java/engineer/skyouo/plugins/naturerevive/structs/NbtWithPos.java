package engineer.skyouo.plugins.naturerevive.structs;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

import org.bukkit.Location;
import org.bukkit.World;

public class NbtWithPos {
    private CompoundTag nbt;
    private Location location;

    public NbtWithPos(String nbt, Location location) {
        try {
            this.nbt = TagParser.parseTag(nbt);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }

        this.location = location;
    }

    public NbtWithPos(String nbt, World world, int x, int y, int z) {
        try {
            this.nbt = TagParser.parseTag(nbt);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }

        this.location = new Location(world, x, y, z);
    }

    public CompoundTag getNbt() {
        return nbt;
    }

    public Location getLocation() {
        return location;
    }
}
