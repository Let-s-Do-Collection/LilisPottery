package net.satisfy.earthernware.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.satisfy.earthernware.client.util.ClientUtil;
import net.satisfy.earthernware.core.block.entity.AbstractStorageBlockEntity;

public class PlanterRenderer implements AbstractStorageTypeRenderer {
    @Override
    public void render(AbstractStorageBlockEntity entity, PoseStack matrices, MultiBufferSource vertexConsumers, NonNullList<ItemStack> itemStacks) {
        for (int slotIndex = 0; slotIndex < itemStacks.size(); slotIndex++) {
            ItemStack stack = itemStacks.get(slotIndex);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) continue;

            matrices.pushPose();
            matrices.translate(-0.8f, 0.05f, -0.5f * slotIndex);
            ClientUtil.renderBlockFromItem(blockItem, matrices, vertexConsumers, entity);
            matrices.popPose();
        }
    }
}
