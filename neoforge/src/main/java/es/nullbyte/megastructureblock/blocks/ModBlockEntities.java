package es.nullbyte.megastructureblock.blocks;


import es.nullbyte.megastructureblock.blocks.blockentities.MegaStructureBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

import static es.nullbyte.megastructureblock.Constants.MOD_ID;
import static es.nullbyte.megastructureblock.blocks.blockentities.ModBlockEntityDefintions.initMegastructureBE;

public class ModBlockEntities {

    //DeferredRegister for blocks
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

    public  static  void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

    static {
        initMegastructureBE(BLOCK_ENTITIES.register("megastructure_block_entity",
                () -> new BlockEntityType<>(MegaStructureBlockEntity::new,
                        Set.of(ModBlockDefintions.MEGASTRUCTURE_BLOCK.get()))));

    }

}
