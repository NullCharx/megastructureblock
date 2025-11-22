package es.nullbyte.megastructureblock.blocks;

import es.nullbyte.nullutils.OnceSupplier;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * This class generalizes blocks registration by making the common class supplier available for all mods.
 * This class only provides the code with a lazy reference that will be dynamically loaded by each mod at the time
 * of bootstrapping. Due to class inheritance, this list can hold any arbitray child class of supplier like
 * RegistryObject Block  to be casted in ML code when needed.
 */
public class ModBlockDefintions {
    public static OnceSupplier<Block> MEGASTRUCTURE_BLOCK= new OnceSupplier<>();

    // Initialize the block reference method (will be called by modloader-specific code)

    public static void initMegaStructureBlock(Supplier<Block> blockSupplier) {MEGASTRUCTURE_BLOCK.set(blockSupplier);}
}
