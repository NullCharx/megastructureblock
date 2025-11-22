package es.nullbyte.megastructureblock.blocks.blockentities;

import es.nullbyte.nullutils.OnceSupplier;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

/**
 * This class generalizes blocks registration by making the common class supplier available for all mods.
 * This class only provides the code with a lazy reference that will be dynamically loaded by each mod at the time
 * of bootstrapping. Due to class inheritance, this list can hold any arbitray child class of supplier like
 * RegistryObject Block to be casted in ML code when needed.
 */
public class ModBlockEntityDefintions {

    public static OnceSupplier<BlockEntityType<?>> MEGASTRUCTURE_BLOCK_ENTITY= new OnceSupplier<>();

    public static void initMegastructureBE(Supplier<BlockEntityType<?>> blockSupplier) {
        MEGASTRUCTURE_BLOCK_ENTITY.set(blockSupplier);
    }


}
