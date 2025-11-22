package es.nullbyte.megastructureblock.networking.packets;

import es.nullbyte.megastructureblock.networking.packets.payloads.MegaStructureDataPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static es.nullbyte.megastructureblock.Constants.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class ClientNetworkingEvent {

    @SubscribeEvent // on the mod event bus
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                MegaStructureDataPayload.TYPE,
                MegaStructureDataPayload.STREAM_CODEC,
                MegaStructureDataPayload::handle

        );


    }
}
