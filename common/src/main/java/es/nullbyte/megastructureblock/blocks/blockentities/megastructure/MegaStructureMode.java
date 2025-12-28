package es.nullbyte.megastructureblock.blocks.blockentities.megastructure;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum MegaStructureMode implements StringRepresentable {
    SAVE("autochunk_save"),
    LOAD("load"),
    CRNR("corner"),
    DATA("data");

    public static final Codec<MegaStructureMode> CODEC = StringRepresentable.fromEnum(MegaStructureMode::values);

    private final String name;
    private final Component displayName;

    MegaStructureMode(String name) {
        this.name = name;
        this.displayName = Component.translatable("megastructure_block.mode_info." + name);
    }

    public String getSerializedName() {
        return this.name;
    }

    public Component getDisplayName() {
        return this.displayName;
    }
}
