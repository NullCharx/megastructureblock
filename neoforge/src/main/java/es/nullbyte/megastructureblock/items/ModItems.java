package es.nullbyte.megastructureblock.items;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import static es.nullbyte.megastructureblock.Constants.MOD_ID;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);



    static {

        //initHexPrototype(ITEMS.register("hexporter_prototype",
         //       () -> new ProtoHexPorter(new Item.Properties().useItemDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,  "hexporter_prototype"))))));
    }
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
