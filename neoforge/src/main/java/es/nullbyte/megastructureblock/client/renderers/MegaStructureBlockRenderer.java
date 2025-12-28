package es.nullbyte.megastructureblock.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import es.nullbyte.megastructureblock.blocks.blockentities.MegaStructureBlockEntity;
import es.nullbyte.megastructureblock.blocks.blockentities.megastructure.MegaStructureMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.joml.Matrix4f;
import net.minecraft.gizmos.Gizmos;
public class MegaStructureBlockRenderer implements BlockEntityRenderer<MegaStructureBlockEntity, MegaStructureBlockRendererState> {
    public MegaStructureBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 200;
    }

    @Override
    public MegaStructureBlockRendererState createRenderState() {
        // Create the render state used to submit the block entity to the feature renderer
        return new MegaStructureBlockRendererState();
    }

    @Override
    public void extractRenderState(MegaStructureBlockEntity blockEntity, MegaStructureBlockRendererState renderState,
                                   float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {

        // Extract the necessary rendering values from the block entity to the render state
        // Always call super or BlockEntityRenderState#extractBase
        MegaStructureBlockRendererState.extractBase(blockEntity, renderState, crumblingOverlay);

        MegaStructureBlockEntity mgst = blockEntity;
        renderState.structurePos = mgst.getStructurePos();
        renderState.structureSize = mgst.getStructureSize();
        renderState.mirror = mgst.getMirror();
        renderState.rotation = mgst.getRotation();
        renderState.mode = mgst.getMode();
        renderState.showBoundingBox = mgst.getShowBoundingBox();
        renderState.showAir = mgst.getShowAir();
        renderState.blockPos = mgst.getBlockPos();
        renderState.level = mgst.getLevel();

        // Populate any desired values
        renderState.partialTick = partialTick;
    }


    @Override
    public void submit(
            MegaStructureBlockRendererState renderState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState
    ) {
        // Only render if player can use GM blocks or is spectator
        if (!(Minecraft.getInstance().player.canUseGameMasterBlocks()
                || Minecraft.getInstance().player.isSpectator())) {
            return;
        }

        Vec3i size = renderState.structureSize;
        if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1) return;

        poseStack.pushPose();

        /* ------------------------------------------------------------
         * Base position (world-space, block-aligned)
         * ------------------------------------------------------------ */
        BlockPos base = renderState.blockPos.offset(renderState.structurePos);

        double minX = base.getX();
        double minY = base.getY();
        double minZ = base.getZ();

        double maxY = minY + size.getY();

        int factor = renderState.mode == MegaStructureMode.LOAD ? 16 : 1;

        /* ------------------------------------------------------------
         * EXTENTS (this is the critical fix)
         * ------------------------------------------------------------ */
        double dx = size.getX() * factor;
        double dz = size.getZ() * factor;

        /* ------------------------------------------------------------
         * Mirror (affects extents sign only)
         * ------------------------------------------------------------ */
        if (renderState.mirror == Mirror.LEFT_RIGHT) {
            dz = -dz;
        } else if (renderState.mirror == Mirror.FRONT_BACK) {
            dx = -dx;
        }

        /* ------------------------------------------------------------
         * Rotation (rotate EXTENTS, not world coordinates)
         * ------------------------------------------------------------ */
        double rdx = dx;
        double rdz = dz;

        switch (renderState.rotation) {
            case CLOCKWISE_90 -> {
                rdx = -dz;
                rdz = dx;
            }
            case CLOCKWISE_180 -> {
                rdx = -dx;
                rdz = -dz;
            }
            case COUNTERCLOCKWISE_90 -> {
                rdx = dz;
                rdz = -dx;
            }
            default -> {
                // NONE
            }
        }

        /* ------------------------------------------------------------
         * Rebuild AABB safely
         * ------------------------------------------------------------ */
        double maxX = minX + rdx;
        double maxZ = minZ + rdz;

        AABB structureBox = new AABB(
                Math.min(minX, maxX),
                minY,
                Math.min(minZ, maxZ),
                Math.max(minX, maxX),
                maxY,
                Math.max(minZ, maxZ)
        );

        /* ------------------------------------------------------------
         * Render structure bounding box
         * ------------------------------------------------------------ */
        if (renderState.mode == MegaStructureMode.SAVE || renderState.showBoundingBox) {
            Gizmos.cuboid(
                    structureBox,
                    GizmoStyle.stroke(0xE5E5E5FF)
            ).setAlwaysOnTop();
        }

        /* ------------------------------------------------------------
         * Render invisible blocks (air / void / barrier / light)
         * ------------------------------------------------------------ */
        if (renderState.mode == MegaStructureMode.SAVE && renderState.showAir) {
            BlockGetter level = renderState.level;

            for (BlockPos p : BlockPos.betweenClosed(
                    base,
                    base.offset(size).offset(-1, -1, -1)
            )) {
                BlockState state = level.getBlockState(p);

                int color;
                if (state.isAir()) color = 0x8080FFFF;
                else if (state.is(Blocks.STRUCTURE_VOID)) color = 0xFFBFBFFF;
                else if (state.is(Blocks.BARRIER)) color = 0xFF0000FF;
                else if (state.is(Blocks.LIGHT)) color = 0xFFFF00FF;
                else continue;

                Gizmos.cuboid(
                        new AABB(p).inflate(0.05),
                        GizmoStyle.stroke(color, 1.0f)
                );
            }
        }

        poseStack.popPose();
    }



}

