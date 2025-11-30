package es.nullbyte.megastructureblock.events.clientrenderer;

import es.nullbyte.megastructureblock.blocks.blockentities.MegaStructureBlockEntity;
import es.nullbyte.megastructureblock.blocks.blockentities.ModBlockEntityDefintions;
import es.nullbyte.megastructureblock.client.renderers.MegaStructureBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;


import static es.nullbyte.megastructureblock.Constants.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class ModClientRenderers {

    @SubscribeEvent
    public static void onRegisterBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                (BlockEntityType<? extends MegaStructureBlockEntity>) ModBlockEntityDefintions.MEGASTRUCTURE_BLOCK_ENTITY.get(),
                MegaStructureBlockRenderer::new
        );
    }
}
