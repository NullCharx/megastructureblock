package es.nullbyte.megastructureblock.client.guis;

import es.nullbyte.megastructureblock.blocks.blockentities.MegaStructureBlockEntity;
import es.nullbyte.megastructureblock.client.guis.blocks.megastructureblock.MegaStructureBlockEditScreen;
import net.minecraft.client.Minecraft;

/**
 * Class that delegates screen opening so that Server-side classes
 * dont have to reference client-side screen classes
 */
public class ClientScreens {

    public  static void openMegaStructureBlockSreen(MegaStructureBlockEntity megaStructureBlockEntity)  {
        Minecraft.getInstance().setScreen(new MegaStructureBlockEditScreen(megaStructureBlockEntity));
    }
}
