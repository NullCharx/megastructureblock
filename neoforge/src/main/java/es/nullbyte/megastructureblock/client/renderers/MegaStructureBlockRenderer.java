package es.nullbyte.megastructureblock.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import es.nullbyte.megastructureblock.blocks.blockentities.MegaStructureBlockEntity;
import es.nullbyte.megastructureblock.blocks.blockentities.megastructure.MegaStructureMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class MegaStructureBlockRenderer implements BlockEntityRenderer<MegaStructureBlockEntity> {
    public MegaStructureBlockRenderer(BlockEntityRendererProvider.Context p_173675_) {
    }


    public void render(MegaStructureBlockEntity blockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1, Vec3 vec3) {
        if (Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator()) {
            BlockPos $$6 = blockEntity.getStructurePos();
            Vec3i $$7 = blockEntity.getStructureSize();
            if ($$7.getX() >= 1 && $$7.getY() >= 1 && $$7.getZ() >= 1) {
                if (blockEntity.getMode() == MegaStructureMode.SAVE
                || blockEntity.getMode() == MegaStructureMode.LOAD) {
                    double $$8 = $$6.getX();
                    double $$9 = $$6.getZ();
                    double $$10 = $$6.getY();
                    double $$11 = $$10 + (double)$$7.getY();
                    double $$12;
                    double $$13;
                    switch (blockEntity.getMirror()) {
                        case LEFT_RIGHT:
                            $$12 = $$7.getX();
                            $$13 = -$$7.getZ();
                            break;
                        case FRONT_BACK:
                            $$12 = -$$7.getX();
                            $$13 = $$7.getZ();
                            break;
                        default:
                            $$12 = $$7.getX();
                            $$13 = $$7.getZ();
                    }

                    double $$30;
                    double $$31;
                    double $$32;
                    double $$33;
                    switch (blockEntity.getRotation()) {
                        case CLOCKWISE_90:
                            $$30 = $$13 < (double)0.0F ? $$8 : $$8 + (double)1.0F;
                            $$31 = $$12 < (double)0.0F ? $$9 + (double)1.0F : $$9;
                            $$32 = $$30 - $$13;
                            $$33 = $$31 + $$12;
                            break;
                        case CLOCKWISE_180:
                            $$30 = $$12 < (double)0.0F ? $$8 : $$8 + (double)1.0F;
                            $$31 = $$13 < (double)0.0F ? $$9 : $$9 + (double)1.0F;
                            $$32 = $$30 - $$12;
                            $$33 = $$31 - $$13;
                            break;
                        case COUNTERCLOCKWISE_90:
                            $$30 = $$13 < (double)0.0F ? $$8 + (double)1.0F : $$8;
                            $$31 = $$12 < (double)0.0F ? $$9 : $$9 + (double)1.0F;
                            $$32 = $$30 + $$13;
                            $$33 = $$31 - $$12;
                            break;
                        default:
                            $$30 = $$12 < (double)0.0F ? $$8 + (double)1.0F : $$8;
                            $$31 = $$13 < (double)0.0F ? $$9 + (double)1.0F : $$9;
                            $$32 = $$30 + $$12;
                            $$33 = $$31 + $$13;
                    }

                    float $$34 = 1.0F;
                    float $$35 = 0.9F;
                    float $$36 = 0.5F;
                    if (blockEntity.getMode() == MegaStructureMode.SAVE|| blockEntity.getShowBoundingBox()) {
                        VertexConsumer $$37 = multiBufferSource.getBuffer(RenderType.lines());
                        ShapeRenderer.renderLineBox(poseStack, $$37, $$30, $$10, $$31, $$32, $$11, $$33, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
                    }

                    if (blockEntity.getMode() == MegaStructureMode.SAVE && blockEntity.getShowAir()) {
                        this.renderInvisibleBlocks(blockEntity, multiBufferSource, poseStack);
                    }

                }
            }
        }
    }

    private void renderInvisibleBlocks(MegaStructureBlockEntity blockEntity, MultiBufferSource multiBufferSource, PoseStack poseStack) {
        BlockGetter $$3 = blockEntity.getLevel();
        VertexConsumer $$4 = multiBufferSource.getBuffer(RenderType.lines());
        BlockPos $$5 = blockEntity.getBlockPos();
        BlockPos $$6 = blockEntity.getBlockPos().offset(blockEntity.getStructurePos());

        for(BlockPos $$7 : BlockPos.betweenClosed($$6, $$6.offset(blockEntity.getStructureSize()).offset(-1, -1, -1))) {
            BlockState $$8 = $$3.getBlockState($$7);
            boolean $$9 = $$8.isAir();
            boolean $$10 = $$8.is(Blocks.STRUCTURE_VOID);
            boolean $$11 = $$8.is(Blocks.BARRIER);
            boolean $$12 = $$8.is(Blocks.LIGHT);
            boolean $$13 = $$10 || $$11 || $$12;
            if ($$9 || $$13) {
                float $$14 = $$9 ? 0.05F : 0.0F;
                double $$15 = (float)($$7.getX() - $$5.getX()) + 0.45F - $$14;
                double $$16 = (float)($$7.getY() - $$5.getY()) + 0.45F - $$14;
                double $$17 = (float)($$7.getZ() - $$5.getZ()) + 0.45F - $$14;
                double $$18 = (float)($$7.getX() - $$5.getX()) + 0.55F + $$14;
                double $$19 = (float)($$7.getY() - $$5.getY()) + 0.55F + $$14;
                double $$20 = (float)($$7.getZ() - $$5.getZ()) + 0.55F + $$14;
                if ($$9) {
                    ShapeRenderer.renderLineBox(poseStack, $$4, $$15, $$16, $$17, $$18, $$19, $$20, 0.5F, 0.5F, 1.0F, 1.0F, 0.5F, 0.5F, 1.0F);
                } else if ($$10) {
                    ShapeRenderer.renderLineBox(poseStack, $$4, $$15, $$16, $$17, $$18, $$19, $$20, 1.0F, 0.75F, 0.75F, 1.0F, 1.0F, 0.75F, 0.75F);
                } else if ($$11) {
                    ShapeRenderer.renderLineBox(poseStack, $$4, $$15, $$16, $$17, $$18, $$19, $$20, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
                } else if ($$12) {
                    ShapeRenderer.renderLineBox(poseStack, $$4, $$15, $$16, $$17, $$18, $$19, $$20, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F);
                }
            }
        }

    }

    private void renderStructureVoids(StructureBlockEntity blockEntity, BlockPos pos, Vec3i boxSize, VertexConsumer consumer, PoseStack poseStack) {
        BlockGetter blockgetter = blockEntity.getLevel();
        if (blockgetter != null) {
            BlockPos blockpos = blockEntity.getBlockPos();
            DiscreteVoxelShape discretevoxelshape = new BitSetDiscreteVoxelShape(boxSize.getX(), boxSize.getY(), boxSize.getZ());

            for (BlockPos blockpos1 : BlockPos.betweenClosed(pos, pos.offset(boxSize).offset(-1, -1, -1))) {
                if (blockgetter.getBlockState(blockpos1).is(Blocks.STRUCTURE_VOID)) {
                    discretevoxelshape.fill(blockpos1.getX() - pos.getX(), blockpos1.getY() - pos.getY(), blockpos1.getZ() - pos.getZ());
                }
            }

            discretevoxelshape.forAllFaces((p_397952_, p_397448_, p_397536_, p_397862_) -> {
                float f = 0.48F;
                float f1 = p_397448_ + pos.getX() - blockpos.getX() + 0.5F - 0.48F;
                float f2 = p_397536_ + pos.getY() - blockpos.getY() + 0.5F - 0.48F;
                float f3 = p_397862_ + pos.getZ() - blockpos.getZ() + 0.5F - 0.48F;
                float f4 = p_397448_ + pos.getX() - blockpos.getX() + 0.5F + 0.48F;
                float f5 = p_397536_ + pos.getY() - blockpos.getY() + 0.5F + 0.48F;
                float f6 = p_397862_ + pos.getZ() - blockpos.getZ() + 0.5F + 0.48F;
                ShapeRenderer.renderFace(poseStack, consumer, p_397952_, f1, f2, f3, f4, f5, f6, 0.75F, 0.75F, 1.0F, 0.2F);
            });
        }
    }

    public boolean shouldRenderOffScreen(StructureBlockEntity p_112581_) {
        return true;
    }



    public int getViewDistance() {
        return 96;
    }
}
