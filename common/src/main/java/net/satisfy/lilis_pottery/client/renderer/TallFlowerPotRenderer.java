package net.satisfy.lilis_pottery.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.satisfy.lilis_pottery.client.util.ClientUtil;
import net.satisfy.lilis_pottery.core.block.entity.AbstractStorageBlockEntity;

public class TallFlowerPotRenderer implements AbstractStorageTypeRenderer {

    @Override
    public void render(AbstractStorageBlockEntity entity, PoseStack matrices, MultiBufferSource vertexConsumers, NonNullList<ItemStack> itemStacks) {
        if (itemStacks.isEmpty()) {
            return;
        }

        ItemStack itemStack = itemStacks.get(0);
        if (!(itemStack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        BlockState lowerState = blockItem.getBlock().defaultBlockState();
        ClientUtil.renderBlock(lowerState, matrices, vertexConsumers, entity);

        if (lowerState.getBlock() instanceof DoublePlantBlock) {
            BlockState upperState = lowerState.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
            matrices.pushPose();
            matrices.translate(0.0f, 1.0f, 0.0f);
            ClientUtil.renderBlock(upperState, matrices, vertexConsumers, entity);
            matrices.popPose();
        }
    }
}