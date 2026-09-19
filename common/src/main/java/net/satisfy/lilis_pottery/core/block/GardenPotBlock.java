package net.satisfy.lilis_pottery.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.lilis_pottery.core.registry.StorageTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class GardenPotBlock extends AbstractFlowerPotBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0),
            Block.box(2.0, 8.0, 2.0, 14.0, 11.0, 14.0)
    );


    public GardenPotBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    public ResourceLocation type() {
        return StorageTypeRegistry.TALL_FLOWER_POT;
    }


    public boolean canInsertStack(ItemStack stack) {
        return stack.is(ItemTags.TALL_FLOWERS) || stack.is(ItemTags.SMALL_FLOWERS);
    }


    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }


}

