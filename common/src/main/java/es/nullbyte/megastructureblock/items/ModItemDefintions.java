package es.nullbyte.megastructureblock.items;

/**
 * This class generalizes items registration by making the common class supplier available for all mods.
 * This class only provides the code with a lazy reference that will be dynamically loaded by each mod at the time
 * of bootstrapping. Due to class inheritance, this list can hold any arbitrary child class of supplier like
 * RegistryObject items to be casted in ML code when needed.
 */

public class ModItemDefintions {
//    public static Supplier<Item> RAW_PILTITE_CHUNK;
//
//    public static Supplier<Item> HEXPORTER_PROTOTYPE;
//
//
//    public static Supplier<Item> SHADOW_STEP_SCROLL;
//
//    // Initialize the block reference method (will be called by modloader-specific code)
//    public static void initRawPiltite(Supplier<Item> blockSupplier) {
//        RAW_PILTITE_CHUNK = blockSupplier;
//    }
//
//    public static void initShadowStepScroll(Supplier<Item> blockSupplier) {
//        SHADOW_STEP_SCROLL = blockSupplier;
//    }
//    public static void initHexPrototype(Supplier<Item> blockSupplier) {
//        HEXPORTER_PROTOTYPE = blockSupplier;
//    }

}
