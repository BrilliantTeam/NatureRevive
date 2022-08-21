package engineer.skyouo.plugins.naturerevive.structs;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

import org.bukkit.Location;

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

    public CompoundTag getNbt() {
        return nbt;
    }

    public Location getLocation() {
        return location;
    }
}
