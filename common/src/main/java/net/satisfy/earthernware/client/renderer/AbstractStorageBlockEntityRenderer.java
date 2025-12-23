package net.satisfy.earthernware.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.satisfy.earthernware.core.block.AbstractStorageBlock;
import net.satisfy.earthernware.core.block.entity.AbstractStorageBlockEntity;

import java.util.HashMap;
import java.util.Map;

public class AbstractStorageBlockEntityRenderer {

    private static final Map<ResourceLocation, AbstractStorageTypeRenderer> STORAGE_TYPES = new HashMap<>();
    private static final Map<Block, RenderTransform> BLOCK_TRANSFORMS = new HashMap<>();
    private static final Map<Class<?>, RenderTransform> CLASS_TRANSFORMS = new HashMap<>();
    private static final RenderTransform DEFAULT_TRANSFORM = new RenderTransform(-0.5f, 0.3f, -0.5f, 1.0f, 0.0f);

    public AbstractStorageBlockEntityRenderer() {
    }

    public static void registerStorageType(ResourceLocation name, AbstractStorageTypeRenderer renderer) {
        STORAGE_TYPES.put(name, renderer);
    }

    public static AbstractStorageTypeRenderer getRendererForId(ResourceLocation name) {
        return STORAGE_TYPES.get(name);
    }

    public static void registerClassTransform(Class<?> blockClass, RenderTransform transform) {
        CLASS_TRANSFORMS.put(blockClass, transform);
    }

    public static RenderTransform getTransformForBlock(Block block) {
        RenderTransform direct = BLOCK_TRANSFORMS.get(block);
        if (direct != null) {
            return direct;
        }
        for (Map.Entry<Class<?>, RenderTransform> entry : CLASS_TRANSFORMS.entrySet()) {
            if (entry.getKey().isInstance(block)) {
                return entry.getValue();
            }
        }
        return DEFAULT_TRANSFORM;
    }

    public void renderContent(AbstractStorageBlockEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (entity == null || !entity.hasLevel()) {
            return;
        }

        BlockState blockState = entity.getBlockState();
        if (!(blockState.getBlock() instanceof AbstractStorageBlock abstractStorageBlock)) {
            return;
        }

        NonNullList<ItemStack> itemStacks = entity.getInventory();

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);

        if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        }

        RenderTransform transform = getTransformForBlock(blockState.getBlock());
        applyTransform(poseStack, transform);

        ResourceLocation type = abstractStorageBlock.type();
        AbstractStorageTypeRenderer renderer = getRendererForId(type);
        if (renderer != null) {
            renderer.render(entity, poseStack, bufferSource, itemStacks);
        }

        poseStack.popPose();
    }

    public static void applyTransform(PoseStack poseStack, RenderTransform transform) {
        poseStack.translate(transform.offsetX(), transform.offsetY(), transform.offsetZ());
        if (transform.rotateYDegrees() != 0.0f) {
            poseStack.mulPose(Axis.YP.rotationDegrees(transform.rotateYDegrees()));
        }
        if (transform.scale() != 1.0f) {
            float scale = transform.scale();
            poseStack.scale(scale, scale, scale);
        }
    }

    public record RenderTransform(float offsetX, float offsetY, float offsetZ, float scale, float rotateYDegrees) {
    }
}
