    package es.nullbyte.megastructureblock;


    import net.minecraftforge.fml.common.Mod;
    import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
    import net.minecraftforge.network.SimpleChannel;



    import static es.nullbyte.megastructureblock.Constants.*;

    @Mod(MOD_ID)
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public class RealmsOfRuneterra {

        public static SimpleChannel networkChannel;

        public RealmsOfRuneterra(FMLJavaModLoadingContext context) {

            // This method is invoked by the Forge mod loader when it is ready
            // to load your mod. You can access Forge and Common code in this
            // project

            // FORGE SIDES SETUP----------------------------------------------


            // ---------------------------------------------------------------

            // FORGE BLOCKS AND ITEMS SETUP----------------------------------


            // ---------------------------------------------------------------






            //ModTerrablenderAPI.registerRegions();
            //Commented PVP listen events
            /*
             //Register the class on the event bus so any events it has will be called
             MinecraftForge.EVENT_BUS.register(PvpManager.class);

            //Add listeners for the events we want to listen to. Since this is not an item or blocck, that are managed in
            //The main class, we need to add the listeners here
            MinecraftForge.EVENT_BUS.addListener(PvpManager::onPlayerLoggedIn);
            MinecraftForge.EVENT_BUS.addListener(PvpManager::onLivingAttack);
            MinecraftForge.EVENT_BUS.addListener(this::onChatReceived);
            //MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
            //MinecraftForge.EVENT_BUS.addListener(CustomFogRenderState::onFogDensity);
            //MinecraftForge.EVENT_BUS.addListener(CustomFogRenderState::onFogColors);
            **/
        }




    }