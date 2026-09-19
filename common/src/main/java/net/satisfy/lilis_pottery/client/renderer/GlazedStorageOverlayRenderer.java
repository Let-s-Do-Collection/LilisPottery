package net.satisfy.lilis_pottery.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.lilis_pottery.core.block.entity.AbstractStorageBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GlazedStorageOverlayRenderer implements BlockEntityRenderer<AbstractStorageBlockEntity> {

    @Override
    public void render(AbstractStorageBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        if (!blockEntity.isGlazed()) {
            return;
        }

        renderGlaze(level, blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getGlazeStrength(), poseStack, bufferSource);
    }

    public static void renderGlaze(Level level, BlockPos pos, BlockState state, float glazeStrength, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource) {
        poseStack.pushPose();

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel baseModel = dispatcher.getBlockModel(state);
        BakedModel overlayModel = new OffsetTintQuadsModel(baseModel, 0.0013f);

        int tintColor = resolveTintColor(level, pos, state, baseModel);
        float red = ((tintColor >> 16) & 255) / 255.0f;
        float green = ((tintColor >> 8) & 255) / 255.0f;
        float blue = (tintColor & 255) / 255.0f;

        // Lighten the paint color towards white to simulate a glossy glaze sheen,
        // scaling with how many times the block has been glazed (0..2.0 -> 0..~70%).
        float sheen = Math.min(1.0f, glazeStrength / 2.0f) * 0.7f;
        red += (1.0f - red) * sheen;
        green += (1.0f - green) * sheen;
        blue += (1.0f - blue) * sheen;

        RenderType renderType = ItemBlockRenderTypes.getRenderType(state, false);

        dispatcher.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(renderType), state, overlayModel, red, green, blue, 0x00F000F0, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    private static int resolveTintColor(Level level, BlockPos pos, BlockState state, BakedModel model) {
        RandomSource random = RandomSource.create(0);
        int tintIndex = firstTintIndex(state, model, random);
        if (tintIndex == -1) {
            return 0xFFFFFF;
        }

        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        int color = blockColors.getColor(state, level, pos, tintIndex);
        if (color == -1) {
            return 0xFFFFFF;
        }
        return color & 0xFFFFFF;
    }

    private static int firstTintIndex(BlockState state, BakedModel model, RandomSource random) {
        int tintIndex = findTintIndexInList(model.getQuads(state, null, random));
        if (tintIndex != -1) {
            return tintIndex;
        }

        for (Direction direction : Direction.values()) {
            tintIndex = findTintIndexInList(model.getQuads(state, direction, random));
            if (tintIndex != -1) {
                return tintIndex;
            }
        }

        return -1;
    }

    private static int findTintIndexInList(List<BakedQuad> quads) {
        for (BakedQuad quad : quads) {
            int tintIndex = quad.getTintIndex();
            if (tintIndex != -1) {
                return tintIndex;
            }
        }
        return -1;
    }

    private record OffsetTintQuadsModel(BakedModel parent, float offset) implements BakedModel {

        @Override
            public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource random) {
                List<BakedQuad> quads = parent.getQuads(state, side, random);
                if (quads.isEmpty()) {
                    return quads;
                }

                List<BakedQuad> tinted = new ArrayList<>(quads.size());
                for (BakedQuad quad : quads) {
                    if (quad.getTintIndex() == -1) {
                        continue;
                    }
                    tinted.add(offsetQuad(quad, quad.getDirection(), offset));
                }
                return tinted;
            }

            @Override
            public boolean useAmbientOcclusion() {
                return parent.useAmbientOcclusion();
            }

            @Override
            public boolean isGui3d() {
                return parent.isGui3d();
            }

            @Override
            public boolean usesBlockLight() {
                return parent.usesBlockLight();
            }

            @Override
            public boolean isCustomRenderer() {
                return parent.isCustomRenderer();
            }

            @Override
            public @NotNull net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleIcon() {
                return parent.getParticleIcon();
            }

            @Override
            public @NotNull ItemTransforms getTransforms() {
                return parent.getTransforms();
            }

            @Override
            public @NotNull ItemOverrides getOverrides() {
                return parent.getOverrides();
            }

            private static BakedQuad offsetQuad(BakedQuad quad, Direction direction, float offset) {
                int[] vertices = quad.getVertices().clone();

                float normalX = direction.getStepX();
                float normalY = direction.getStepY();
                float normalZ = direction.getStepZ();

                int stride = 8;
                for (int vertex = 0; vertex < 4; vertex++) {
                    int base = vertex * stride;

                    float x = Float.intBitsToFloat(vertices[base]);
                    float y = Float.intBitsToFloat(vertices[base + 1]);
                    float z = Float.intBitsToFloat(vertices[base + 2]);

                    x += normalX * offset;
                    y += normalY * offset;
                    z += normalZ * offset;

                    vertices[base] = Float.floatToRawIntBits(x);
                    vertices[base + 1] = Float.floatToRawIntBits(y);
                    vertices[base + 2] = Float.floatToRawIntBits(z);
                }

                return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
            }
        }
}