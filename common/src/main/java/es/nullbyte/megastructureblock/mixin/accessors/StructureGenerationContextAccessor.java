package es.nullbyte.megastructureblock.mixin.accessors;

import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


/**
 * Accessor for generation context(es)
 */
@Mixin({Structure.GenerationContext.class})
public interface StructureGenerationContextAccessor {

    /**
     * Accessor of the chunkGenerator used in the context.
     * @return context chunk generator
     */
    @Accessor("chunkGenerator")
    ChunkGenerator getChunkGenerator();


}

