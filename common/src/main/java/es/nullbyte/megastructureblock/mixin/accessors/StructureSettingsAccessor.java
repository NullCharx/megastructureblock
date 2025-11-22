package es.nullbyte.megastructureblock.mixin.accessors;

import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


/**
 * Grabber for structure settings (megastructureS)
 */
@Mixin({Structure.class})
public interface StructureSettingsAccessor {

    @Accessor("settings")
    Structure.StructureSettings getSettings();

}

