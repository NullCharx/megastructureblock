package es.nullbyte.megastructureblock.client.renderers;

import es.nullbyte.megastructureblock.blocks.blockentities.MegaStructureBlockEntity;
import es.nullbyte.megastructureblock.blocks.blockentities.megastructure.MegaStructureMode;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * contains all the information you might need for rendering (and all IMMUTABLE information, so not your stack handler directly)
 */
public class MegaStructureBlockRendererState extends BlockEntityRenderState {

    // General
    public float partialTick;

    // Structure data
    public BlockPos structurePos;
    public Vec3i structureSize;
    public Mirror mirror;
    public Rotation rotation;
    public MegaStructureMode mode;

    // Flags
    public boolean showBoundingBox;
    public boolean showAir;

    // Needed reference positions
    public BlockPos blockPos;  // Position of the BE itself
    public BlockGetter level;  // Reference to world for invisible blocks




    /** Returns the axis-aligned bounding box for the structure, transformed by mirror and rotation */
    public AABB getTransformedBox() {
        double minX = structurePos.getX();
        double minY = structurePos.getY();
        double minZ = structurePos.getZ();
        double maxX = minX + structureSize.getX();
        double maxY = minY + structureSize.getY();
        double maxZ = minZ + structureSize.getZ();

        // Mirror adjustments
        switch (mirror) {
            case LEFT_RIGHT -> maxZ = minZ - structureSize.getZ();
            case FRONT_BACK -> maxX = minX - structureSize.getX();
            default -> {}
        }

        // Rotation adjustments
        double tMinX = minX, tMinZ = minZ, tMaxX = maxX, tMaxZ = maxZ;
        switch (rotation) {
            case CLOCKWISE_90 -> { tMinX = maxZ < 0 ? minX : minX + 1.0; tMinZ = maxX < 0 ? minZ + 1.0 : minZ; tMaxX = tMinX - maxZ; tMaxZ = tMinZ + maxX; }
            case CLOCKWISE_180 -> { tMinX = maxX < 0 ? minX : minX + 1.0; tMinZ = maxZ < 0 ? minZ : minZ + 1.0; tMaxX = tMinX - maxX; tMaxZ = tMinZ - maxZ; }
            case COUNTERCLOCKWISE_90 -> { tMinX = maxZ < 0 ? minX + 1.0 : minX; tMinZ = maxX < 0 ? minZ : minZ + 1.0; tMaxX = tMinX + maxZ; tMaxZ = tMinZ - maxX; }
            default -> { tMinX = maxX < 0 ? minX + 1.0 : minX; tMinZ = maxZ < 0 ? minZ + 1.0 : minZ; tMaxX = tMinX + maxX; tMaxZ = tMinZ + maxZ; }
        }

        return new AABB(tMinX, minY, tMinZ, tMaxX, maxY, tMaxZ);
    }

    /** Returns a list of all invisible or special blocks in the structure for rendering */
    public List<BlockPos> getInvisibleBlocks() {
        if (!showAir || level == null) return List.of();

        List<BlockPos> invisible = new ArrayList<>();
        BlockPos origin = blockPos.offset(structurePos);

        for (BlockPos p : BlockPos.betweenClosed(origin, origin.offset(structureSize).offset(-1, -1, -1))) {
            BlockState state = level.getBlockState(p);
            if (state.isAir() || state.is(Blocks.STRUCTURE_VOID) || state.is(Blocks.BARRIER) || state.is(Blocks.LIGHT)) {
                invisible.add(p);
            }
        }

        return invisible;
    }

    /** Returns RGB for the invisible/special block type */
    public static float[] getBlockColor(BlockState state) {
        if (state.isAir()) return new float[]{0.5f, 0.5f, 1.0f};
        if (state.is(Blocks.STRUCTURE_VOID)) return new float[]{1.0f, 0.75f, 0.75f};
        if (state.is(Blocks.BARRIER)) return new float[]{1.0f, 0.0f, 0.0f};
        if (state.is(Blocks.LIGHT)) return new float[]{1.0f, 1.0f, 0.0f};
        return new float[]{1.0f, 1.0f, 1.0f}; // fallback
    }
}
