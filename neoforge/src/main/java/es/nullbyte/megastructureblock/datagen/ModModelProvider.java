package es.nullbyte.megastructureblock.datagen;

import es.nullbyte.megastructureblock.blocks.ModBlockDefintions;
import es.nullbyte.megastructureblock.blocks.ModBlocks;
import es.nullbyte.megastructureblock.blocks.blockentities.megastructure.MegaStructureMode;
import es.nullbyte.megastructureblock.blocks.megastructures.MegaStructureBlock;
import es.nullbyte.megastructureblock.items.ModItemDefintions;
import es.nullbyte.megastructureblock.items.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

import static es.nullbyte.megastructureblock.Constants.MOD_ID;
import static es.nullbyte.megastructureblock.blocks.ModBlockDefintions.*;
import static net.minecraft.client.data.models.BlockModelGenerators.createSimpleBlock;
import static net.minecraft.client.data.models.BlockModelGenerators.plainVariant;


public class ModModelProvider extends ModelProvider {


    public ModModelProvider(PackOutput output) {
        super(output, MOD_ID);
    }

    //Model Datagen https://docs.neoforged.net/docs/ version /resources/client/models/datagen/ and migration primer
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        TextureMapping stage1Mapping = TextureMapping.cube(Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block"))
                .put(TextureSlot.UP,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block"))
                .put(TextureSlot.DOWN,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block"))
                .put(TextureSlot.WEST,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block"))
                .put(TextureSlot.EAST,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block"));
        TextureMapping stage2Mapping = TextureMapping.cube(Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_data"))
                .put(TextureSlot.UP,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_data"))
                .put(TextureSlot.DOWN,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_data"))
                .put(TextureSlot.WEST,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_data"))
                .put(TextureSlot.EAST,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_data"));
        TextureMapping stage3Mapping = TextureMapping.cube(Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_load"))
                .put(TextureSlot.UP,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_load"))
                .put(TextureSlot.DOWN,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_load"))
                .put(TextureSlot.WEST,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_load"))
                .put(TextureSlot.EAST,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_load"));
        TextureMapping stage4Mapping = TextureMapping.cube(Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_save"))
                .put(TextureSlot.UP,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_save"))
                .put(TextureSlot.DOWN,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_save"))
                .put(TextureSlot.WEST,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_save"))
                .put(TextureSlot.EAST,Identifier.fromNamespaceAndPath(MOD_ID,"block/megastructure_block_save"));

        Identifier stage1ModelId = ModelTemplates.CUBE.create(MEGASTRUCTURE_BLOCK.get(), stage1Mapping, blockModels.modelOutput);
        Identifier stage2ModelId = ModelTemplates.CUBE.createWithSuffix(MEGASTRUCTURE_BLOCK.get(), "_data", stage2Mapping, blockModels.modelOutput);
        Identifier stage3ModelId = ModelTemplates.CUBE.createWithSuffix(MEGASTRUCTURE_BLOCK.get(), "_load", stage3Mapping, blockModels.modelOutput);
        Identifier stage4ModelId = ModelTemplates.CUBE.createWithSuffix(MEGASTRUCTURE_BLOCK.get(), "_save", stage4Mapping, blockModels.modelOutput);

        // Generate blockstate JSON using STAGE property
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(MEGASTRUCTURE_BLOCK.get())
                .with(PropertyDispatch.initial(MegaStructureBlock.MODE)
                        .select(MegaStructureMode.CRNR, plainVariant(stage1ModelId))
                        .select(MegaStructureMode.DATA, plainVariant(stage2ModelId))
                        .select(MegaStructureMode.LOAD, plainVariant(stage3ModelId))
                        .select(MegaStructureMode.SAVE, plainVariant(stage4ModelId))
                )
        );

//        blockModels.blockStateOutput.accept(createSimpleBlock(MEGASTRUCTURE_BLOCK.get(), plainVariant(TexturedModel.CUBE.create(block, this.modelOutput))));

        // Register simple item model (use stage 1)
        itemModels.generateFlatItem(MEGASTRUCTURE_BLOCK.get().asItem(), ModelTemplates.FLAT_ITEM);

    }
    /**
     * Check what items / models need item models
     * @return
     */
    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream();
        //To remove blocks that dont need models (i.e they are complex and generated otside datagen):
        //.filter(x -> !x.is(ModBlocks.BLOCK) && ...)
        //THat is to say, add blocks to the stream that arent these ones
    }
    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return ModItems.ITEMS.getEntries().stream();
        //To remove blocks that dont need models (i.e they are complex and generated otside datagen):
        //.filter(x -> x.get != ModBlocks.BLOCK.asItem() && ...)
        //THat is to say, add items to the stream that arent these ones
    }
}
