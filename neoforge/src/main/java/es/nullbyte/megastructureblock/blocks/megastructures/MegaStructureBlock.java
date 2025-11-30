package es.nullbyte.megastructureblock.blocks.megastructures;

import com.mojang.serialization.MapCodec;
import es.nullbyte.megastructureblock.blocks.blockentities.MegaStructureBlockEntity;
import es.nullbyte.megastructureblock.blocks.blockentities.megastructure.MegaStructureMode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MegaStructureBlock extends BaseEntityBlock implements GameMasterBlock {
    public static final MapCodec<MegaStructureBlock> CODEC = simpleCodec(MegaStructureBlock::new); //Codec for data
    public static final EnumProperty<MegaStructureMode> MODE = EnumProperty.create("mode", MegaStructureMode.class); //Mode enum


    /**
     * Codec method
     * @return
     */
    @Override
    public MapCodec<MegaStructureBlock> codec() {
        return CODEC;
    }

    /**
     * COnstructor
     * @param blockProperties
     */
    public MegaStructureBlock(Properties blockProperties) {
        super(blockProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MODE, MegaStructureMode.LOAD));
    }

    /**
     * Return an instance of this block's entity.
     * @param blockPos
     * @param blockState
     * @return
     */
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MegaStructureBlockEntity(blockPos,blockState);
    }

    /**
     * Add the property to the block.
     *
     * @param builder
     */
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    /**
     * Interaction when using without an item in hand.
     *
     * @param state
     * @param level
     * @param pos
     * @param player
     * @param hitResult
     * @return
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MegaStructureBlockEntity) {
            return ((MegaStructureBlockEntity) blockEntity).usedBy(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide()) {
            if (placer != null) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof MegaStructureBlockEntity) {
                    ((MegaStructureBlockEntity) blockEntity).createdBy(placer);
                }
            }

        }
    }


    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level instanceof ServerLevel) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MegaStructureBlockEntity structureBlockEntity) {
                boolean bl = level.hasNeighborSignal(pos);
                boolean bl2 = structureBlockEntity.isPowered();
                if (bl && !bl2) {
                    structureBlockEntity.setPowered(true);
                    this.trigger((ServerLevel) level, structureBlockEntity);
                } else if (!bl && bl2) {
                    structureBlockEntity.setPowered(false);
                }

            }
        }
    }

    private void trigger(ServerLevel level, MegaStructureBlockEntity blockEntity) {
        switch (blockEntity.getMode()) {
            case SAVE:
                blockEntity.saveStructure(false);
                break;
            case LOAD:
                blockEntity.placeStructure(level);
                break;
            case CRNR:
                blockEntity.unloadStructure();
            case DATA:
        }

    }



}
