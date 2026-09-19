package net.satisfy.lilis_pottery.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.Level;
import net.satisfy.lilis_pottery.core.block.entity.UrnBlockEntity;
import org.jetbrains.annotations.NotNull;

public class UrnGlazeOverlayRenderer implements BlockEntityRenderer<UrnBlockEntity> {

    @Override
    public void render(UrnBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        if (!blockEntity.isGlazed()) {
            return;
        }

        GlazedStorageOverlayRenderer.renderGlaze(level, blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getGlazeStrength(), poseStack, bufferSource);
    }
}
