package es.nullbyte.megastructureblock.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import static es.nullbyte.megastructureblock.Constants.MOD_ID;

public class CreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MOD_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    public static void register(IEventBus eventBus) {
        CREATIVE_MOD_TABS.register(eventBus);
    }

    //Avid TP items tab. The order in which the tabs are registered is the order they will appear in the creative menu
    //The order in which the items are registered is the order they will appear in the tab

//    public static final Supplier<CreativeModeTab> PILTOVER_ITEMS = CREATIVE_MOD_TABS.register("pilt_items",
//            () -> CreativeModeTab.builder()
//                    .icon(() -> new ItemStack(PILTITE_ORE.get().asItem())) //Use one of the items as icon
//                    .title(Component.translatable("creativetab.pilt_items")) //Translation key for the tab name (lang file)
//                    .displayItems((pParameters, pOutput) -> {   //List of items to display in the tab
//                        pOutput.accept(PILTITE_ORE.get());
//                        pOutput.accept(RAW_PILTITE_CHUNK.get());
//                        pOutput.accept(REFINED_PILTITE.get());
//                        pOutput.accept(PILTSTONE_BASE.get());
//                    }).build());
//
//    public static final Supplier<CreativeModeTab> MAGIC_ELEMENTS = CREATIVE_MOD_TABS.register("general_magic_elements",
//            () -> CreativeModeTab.builder()
//                    .icon(() -> new ItemStack(ANCIENT_MINERAL.get().asItem())) //Use one of the items as icon
//                    .title(Component.translatable("creativetab.magic_items")) //Translation key for the tab name (lang file)
//                    .displayItems((pParameters, pOutput) -> {   //List of items to display in the tab
//                        pOutput.accept(ANCIENT_MINERAL.get());
//                        pOutput.accept(ANCIENT_RUNIC_HARDENED_MINERAL.get());
//                        pOutput.accept(SHADOW_STEP_SCROLL.get());
//                        //pOutput.accept(HEXPORTER_PROTOTYPE.get());
//                    }).build());


    /*
    public static final RegistryObject<CreativeModeTab> AVID_DECOR = CREATIVE_MOD_TABS.register("avid_decor",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.ABERRANT_BLOCK.get())) //Use one of the items as icon
                    .title(Component.translatable("creativetab.aviddecor")) //Translation key for the tab name (lang file)
                    .displayItems((pParameters, pOutput) -> {   //List of items to display in the tab. Items are ordered in the order they are added
                        pOutput.accept(ModBlocks.BIG_DIMENSIONAL_BATTERY.get());

                        //You can also add vanilla items to the tab
                    })
                    .build());
                    */


}