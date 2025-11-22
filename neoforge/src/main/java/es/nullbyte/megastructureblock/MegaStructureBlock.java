package es.nullbyte.megastructureblock;

import es.nullbyte.megastructureblock.blocks.ModBlockEntities;
import es.nullbyte.megastructureblock.blocks.ModBlocks;
import es.nullbyte.megastructureblock.items.CreativeModTabs;
import es.nullbyte.megastructureblock.items.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import static es.nullbyte.megastructureblock.Constants.MOD_LOGGER;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Mod(Constants.MOD_ID)
public class MegaStructureBlock {

    public MegaStructureBlock(IEventBus modEventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.MOD_LOGGER.info("Hello NeoForge world!");
        CommonClass.init();
        modEventBus.addListener(this::preInit);

        // Neo client server events


        // NEOFORGE BLOCKS AND ITEMS SETUP----------------------------------
        ModBlocks.register(modEventBus); //Register blocks
        ModBlockEntities.register(modEventBus); //Register block entities
        ModItems.register(modEventBus); //Register Items
        CreativeModTabs.register(modEventBus); //Ivnentory tag
        // -----------------------------------------------------------------

        //Worldgen registires-----------------------------------------------

        // -----------------------------------------------------------------
        //Custom type deferred registries.
        //ModNeoDeferredRegistries.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        EVENT_BUS.register(this);

        //Sounds
        //Particle types
        //ModParticleTypes.register(modEventBus);




    }
// In some client-only event class

    /**
     * Client and server setup (Common synced phase)
     * @param event
     */
    public void preInit(FMLCommonSetupEvent event) {

    }

    /**
     * Client side setup:
     * - Set render layers for translucent blocks (TODO)
     * - Add custom dimension effects to clien renderer.
     * @param event
     */
    public void onClientSetup(FMLClientSetupEvent event) {
        //The client must subscribe to the event that renders translucent blocks

        //EVENT_BUS.addListener(this::registerTransparency);


    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        MOD_LOGGER.info("---- MEGASTRUCTUREBLOCK mod-------.");
        MOD_LOGGER.info("Welcome... To the summoner- Wait what");
        MOD_LOGGER.info("--------------------------------");


    }


}