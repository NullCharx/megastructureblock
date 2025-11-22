package es.nullbyte.megastructureblock.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

import static es.nullbyte.megastructureblock.Constants.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class DataGenerators {
    //Class that registers the data generators
    //in this case we will only add the item model provider, which the one we have items to test with
    //To remember everything and data gen for 1.20.x, check https://www.youtube.com/watch?v=enzKJWq0vNI&list=PLKGarocXCE1H9Y21-pxjt5Pt8bW14twa-
    //When you add new data generators or new data to an existing provider you must run gradlew runData to generate the data
    @SubscribeEvent
    public static  void gatherClientData(GatherDataEvent.Client event) {
        System.out.println("a-aaaaaaaaaaaa");
        gatherSidedData(event);

    }

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        System.out.println("a-aaaaaaaaaaaa");

        gatherSidedData(event);

    }

    private static void gatherSidedData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        //Add the providers


        //Block state and tag providerss
        generator.addProvider(true, new ModModelProvider(output)); //Loot table provider




    }

}
