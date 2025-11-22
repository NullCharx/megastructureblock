package es.nullbyte.megastructureblock.datagen;

import es.nullbyte.megastructureblock.blocks.ModBlocks;
import es.nullbyte.megastructureblock.items.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

import static es.nullbyte.megastructureblock.Constants.MOD_ID;
import static es.nullbyte.megastructureblock.blocks.ModBlockDefintions.*;


public class ModModelProvider extends ModelProvider {


    public ModModelProvider(PackOutput output) {
        super(output, MOD_ID);
    }

    //Model Datagen https://docs.neoforged.net/docs/ version /resources/client/models/datagen/ and migration primer
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        //Interesting ExtendedModelTemplate:
        //FLAT HADNHELD ITEM: Swords, pickaxes....
        //ARMORS: TrimmableItem (Armor part, armor Material, name of material, dyeable)
        //BOW: Flat item model first, then call generateBow witht he same bow item
        //Piltite Ore


        blockModels.createTrivialBlock(MEGASTRUCTURE_BLOCK.get(), TexturedModel.CUBE);
        blockModels.createFlatItemModelWithBlockTexture(MEGASTRUCTURE_BLOCK.get().asItem(),MEGASTRUCTURE_BLOCK.get());

        //complexBlock(REFINED_PILTITE.get());
        //exampleRotatingDatagen(ModItems.HEXPORTER_PROTOTYPE);
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
