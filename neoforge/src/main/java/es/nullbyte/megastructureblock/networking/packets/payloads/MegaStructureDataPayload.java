package es.nullbyte.megastructureblock.networking.packets.payloads;

import es.nullbyte.megastructureblock.blocks.blockentities.MegaStructureBlockEntity;
import es.nullbyte.megastructureblock.blocks.blockentities.megastructure.MegaStructureMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static es.nullbyte.megastructureblock.Constants.MOD_ID;
import static es.nullbyte.megastructureblock.networking.packets.ModNetwork.*;

public record MegaStructureDataPayload(
        BlockPos pos,
        MegaStructureBlockEntity.UpdateType updateType,
        MegaStructureMode mode,
        String name,
        BlockPos offset,
        Vec3i size,
        Mirror mirror,
        Rotation rotation,
        String data,
        boolean ignoreEntities,
        boolean showAir,
        boolean showBoundingBox,
        float integrity,
        long seed
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MegaStructureDataPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "megasturcture_data_payload"));
    private static final int FLAG_IGNORE_ENTITIES = 1;
    private static final int FLAG_SHOW_AIR = 2;
    private static final int FLAG_SHOW_BOUNDING_BOX = 4;

    public static final StreamCodec<ByteBuf, MegaStructureDataPayload> STREAM_CODEC = composite14(
            BlockPos.STREAM_CODEC,
            MegaStructureDataPayload::pos,
            MEGASTRUCTURE_UPDATE_MODE_STREAM_CODEC,
            MegaStructureDataPayload::updateType,
            MEGASTRUCTURE_MODE_STREAM_CODEC,
            MegaStructureDataPayload::mode,
            ByteBufCodecs.STRING_UTF8,
            MegaStructureDataPayload::name,
            BlockPos.STREAM_CODEC,
            MegaStructureDataPayload::offset,
            VEC3I_STREAM_CODEC,
            MegaStructureDataPayload::size,
            MIRROR_STREAM_CODEC,
            MegaStructureDataPayload::mirror,
            ROTATION_STREAM_CODEC,
            MegaStructureDataPayload::rotation,
            ByteBufCodecs.STRING_UTF8,
            MegaStructureDataPayload::data,
            ByteBufCodecs.BOOL,
            MegaStructureDataPayload::ignoreEntities,
            ByteBufCodecs.BOOL,
            MegaStructureDataPayload::showAir,
            ByteBufCodecs.BOOL,
            MegaStructureDataPayload::showBoundingBox,
            ByteBufCodecs.FLOAT,
            MegaStructureDataPayload::integrity,
            ByteBufCodecs.LONG,
            MegaStructureDataPayload::seed,
            MegaStructureDataPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final MegaStructureDataPayload data, final IPayloadContext ctx) {
        // Do something with the data, on the main thread
        ctx.enqueueWork(() -> {
            if (ctx.player().canUseGameMasterBlocks()) {
                BlockPos pos = data.pos;
                Player player = ctx.player();
                Level level = player.level();
                BlockState blockState = level.getBlockState(pos);
                BlockEntity structureEntity = level.getBlockEntity(pos);
                if (structureEntity instanceof MegaStructureBlockEntity) {
                    MegaStructureBlockEntity structureBlockEntity = getMegaStructureBlockEntity(data, (MegaStructureBlockEntity) structureEntity);
                    if (structureBlockEntity.hasStructureName()) {
                        String structureName = structureBlockEntity.getStructureName();
                        if (data.updateType == MegaStructureBlockEntity.UpdateType.SAVE_AREA) {
                            System.out.println("Opposite Corner = " + pos.getX()+data.size.getX() + "," + pos.getY()+data.size.getY() + "," +pos.getZ()+data.size.getZ());
                            if (structureBlockEntity.saveStructure()) {
                                player.displayClientMessage(Component.translatable("structure_block.save_success", structureName), false);
                            } else {
                                player.displayClientMessage(Component.translatable("structure_block.save_failure", structureName), false);
                            }
                        } else if (data.updateType == MegaStructureBlockEntity.UpdateType.LOAD_AREA) {
                            if (!structureBlockEntity.isStructureLoadable()) {
                                player.displayClientMessage(Component.translatable("structure_block.load_not_found", structureName), false);
                            } else if (structureBlockEntity.placeStructureIfSameSize((ServerLevel) level)) {
                                player.displayClientMessage(Component.translatable("structure_block.load_success", structureName), false);
                            } else {
                                player.displayClientMessage(Component.translatable("structure_block.load_prepare", structureName), false);
                            }
                        } else if (data.updateType == MegaStructureBlockEntity.UpdateType.SCAN_AREA) {
                            if (structureBlockEntity.detectSize()) {
                                player.displayClientMessage(Component.translatable("structure_block.size_success", structureName), false);
                            } else {
                                player.displayClientMessage(Component.translatable("structure_block.size_failure"), false);
                            }
                        }
                    } else {
                        player.displayClientMessage(Component.translatable("structure_block.invalid_structure_name", data.name), false);
                    }

                    structureBlockEntity.setChanged();
                    player.level().sendBlockUpdated(pos, blockState, blockState, 3);
                }
            }
        }).exceptionally(e -> {
            // Handle exception
            ctx.disconnect(Component.literal("Spmething went wrong handling megasturcture block data:" + e));
            return null;
        });
    }


    private static @NotNull MegaStructureBlockEntity getMegaStructureBlockEntity(MegaStructureDataPayload packet, MegaStructureBlockEntity structureEntity) {
        MegaStructureBlockEntity structureBlockEntity = structureEntity;
        structureBlockEntity.setMode(packet.mode);
        structureBlockEntity.setStructureName(packet.name);
        structureBlockEntity.setStructurePos(packet.offset);
        structureBlockEntity.setStructureSize(packet.size);
        structureBlockEntity.setMirror(packet.mirror);
        structureBlockEntity.setRotation(packet.rotation);
        structureBlockEntity.setMetaData(packet.data);
        structureBlockEntity.setIgnoreEntities(packet.ignoreEntities);
        structureBlockEntity.setShowAir(packet.showAir);
        structureBlockEntity.setShowBoundingBox(packet.showBoundingBox);
        structureBlockEntity.setIntegrity(packet.integrity);
        structureBlockEntity.setSeed(packet.seed);
        return structureBlockEntity;
    }
}