package es.nullbyte.megastructureblock.blocks;

import es.nullbyte.megastructureblock.blocks.megastructures.MegaStructureBlock;
import es.nullbyte.megastructureblock.items.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

import static es.nullbyte.megastructureblock.Constants.MOD_ID;
import static es.nullbyte.megastructureblock.blocks.ModBlockDefintions.*;

/**
 * Register blocks defined and saved on the common block definitions
 * <a href="https://docs.neoforged.net/docs/blocks">Block definitions</a>
 */
public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);

    static {

        initMegaStructureBlock(registerBlock("megastructure_block",
                () -> new MegaStructureBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRUCTURE_BLOCK)
                        .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID,"megastructure_block")))
                )
        ));

    }


    /**
     * For blocks that aren't supposed to be pickable (i.e fire)
     * @param name
     * @param block
     * @return
     * @param <T>
     */
    private static <T extends Block> Supplier<T> registerBlockNoItem(String name, Supplier<T> block) {
        //T is the block type. It will register the block and the block item.
        Supplier<T> returnBlock = BLOCKS.register(name, block); //Register the block
        return returnBlock; //Return the block
    }

    /**
     * Registers block and block item
     * @param name
     * @param block
     * @return
     * @param <T>
     */
    private static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block, ChatFormatting ... toolTipformat) {
        //T is the block type. It will register the block and the block item.
        Supplier<T> returnBlock = BLOCKS.register(name, block); //Register the block
        registerBlockItem(name, returnBlock, toolTipformat); //Register the block item
        return returnBlock; //Return the block
    }
    private static <T extends Block> Supplier<Item> registerBlockItem(String name, Supplier<T> block, ChatFormatting ... toolTipformat) {
        //T is the block type. It will register the block item
        return ModItems.ITEMS.register(name, () ->
                new BlockItem(block.get(),
                        new Item.Properties()
                                .useBlockDescriptionPrefix()
                                .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name)))
                                .component(DataComponents.LORE, new ItemLore(List.of(Component.translatable("block." + MOD_ID+ "." + name + ".tooltip")
                                        .withStyle(toolTipformat)))
                )));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
