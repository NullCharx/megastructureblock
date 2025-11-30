package es.nullbyte.megastructureblock.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import es.nullbyte.megastructureblock.blocks.blockentities.MegaStructureBlockEntity;
import es.nullbyte.megastructureblock.blocks.blockentities.megastructure.MegaStructureMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.joml.Matrix4f;

public class MegaStructureBlockRenderer implements BlockEntityRenderer<MegaStructureBlockEntity, MegaStructureBlockRendererState> {
    public MegaStructureBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
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
        if (!(Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator())) {
            return;
        }

        // Validate structure size
        Vec3i size = renderState.structureSize;
        if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1) return;

        poseStack.pushPose();

        BlockPos pos = renderState.structurePos;
        double minX = pos.getX();
        double minY = pos.getY();
        double minZ = pos.getZ();
        double maxY = minY + size.getY();
        int factor = renderState.mode == MegaStructureMode.LOAD ? 16 : 1;
        double maxX = minX + (size.getX() * factor);
        double maxZ = minZ + (size.getX() * factor);

        // Apply mirror adjustments
        switch (renderState.mirror) {
            case LEFT_RIGHT -> maxZ = minZ - size.getZ();
            case FRONT_BACK -> maxX = minX - size.getX();
            default -> {}
        }

        // Apply rotation adjustments
        double tMinX = minX, tMinZ = minZ, tMaxX = maxX, tMaxZ = maxZ;
        switch (renderState.rotation) {
            case CLOCKWISE_90 -> {
                tMinX = maxZ < 0 ? minX : minX + 1.0;
                tMinZ = maxX < 0 ? minZ + 1.0 : minZ;
                tMaxX = tMinX - maxZ;
                tMaxZ = tMinZ + maxX;
            }
            case CLOCKWISE_180 -> {
                tMinX = maxX < 0 ? minX : minX + 1.0;
                tMinZ = maxZ < 0 ? minZ : minZ + 1.0;
                tMaxX = tMinX - maxX;
                tMaxZ = tMinZ - maxZ;
            }
            case COUNTERCLOCKWISE_90 -> {
                tMinX = maxZ < 0 ? minX + 1.0 : minX;
                tMinZ = maxX < 0 ? minZ : minZ + 1.0;
                tMaxX = tMinX + maxZ;
                tMaxZ = tMinZ - maxX;
            }
            default -> {
                tMinX = maxX < 0 ? minX + 1.0 : minX;
                tMinZ = maxZ < 0 ? minZ + 1.0 : minZ;
                tMaxX = tMinX + maxX;
                tMaxZ = tMinZ + maxZ;
            }
        }

        // Draw bounding box if needed
        if (renderState.mode == MegaStructureMode.SAVE || renderState.showBoundingBox) {
            double finalTMinX = tMinX;
            double finalTMinZ = tMinZ;
            double finalTMaxX = tMaxX;
            double finalTMaxZ = tMaxZ;
            collector.submitCustomGeometry(poseStack, RenderType.lines(), (pose, consumer) -> {
                ShapeRenderer.renderLineBox(pose, consumer, finalTMinX, minY, finalTMinZ, finalTMaxX, maxY, finalTMaxZ, 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
            });
        }

        // Draw invisible blocks if needed
        if (renderState.mode == MegaStructureMode.SAVE && renderState.showAir) {
            BlockGetter world = renderState.level;
            BlockPos origin = renderState.blockPos.offset(pos);

            collector.submitCustomGeometry(poseStack, RenderType.lines(), (pose, consumer) -> {
                for (BlockPos p : BlockPos.betweenClosed(origin, origin.offset(size).offset(-1, -1, -1))) {
                    BlockState state = world.getBlockState(p);
                    float r, g, b;
                    if (state.isAir()) { r = 0.5f; g = 0.5f; b = 1.0f; }
                    else if (state.is(Blocks.STRUCTURE_VOID)) { r = 1.0f; g = 0.75f; b = 0.75f; }
                    else if (state.is(Blocks.BARRIER)) { r = 1.0f; g = 0.0f; b = 0.0f; }
                    else if (state.is(Blocks.LIGHT)) { r = 1.0f; g = 1.0f; b = 0.0f; }
                    else continue;

                    double minXb = p.getX() - renderState.blockPos.getX() + 0.45;
                    double minYb = p.getY() - renderState.blockPos.getY() + 0.45;
                    double minZb = p.getZ() - renderState.blockPos.getZ() + 0.45;
                    double maxXb = p.getX() - renderState.blockPos.getX() + 0.55;
                    double maxYb = p.getY() - renderState.blockPos.getY() + 0.55;
                    double maxZb = p.getZ() - renderState.blockPos.getZ() + 0.55;

                    ShapeRenderer.renderLineBox(pose, consumer, minXb, minYb, minZb, maxXb, maxYb, maxZb, r, g, b, 1.0f);
                }
            });
        }

        poseStack.popPose();

    }


}

