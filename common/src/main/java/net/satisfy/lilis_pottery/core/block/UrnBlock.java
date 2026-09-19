package net.satisfy.lilis_pottery.core.block;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.lilis_pottery.core.block.entity.UrnBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.satisfy.lilis_pottery.core.block.AbstractStorageBlock.nearestDyeColor;

public class UrnBlock extends AbstractFacingBlock implements EntityBlock {
    public enum Variant {
        URN,
        AMPHORE
    }

    public static final BooleanProperty PAINTED = AbstractFlowerPotBlock.PAINTED;

    private static final VoxelShape URN_SHAPE = Shapes.or(
            Block.box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0),
            Block.box(5.0, 10.0, 5.0, 11.0, 14.0, 11.0),
            Block.box(4.0, 14.0, 4.0, 12.0, 16.0, 12.0)
    );

    private static final VoxelShape AMPHORE_SHAPE = Shapes.or(
            Block.box(5.0, 0.0, 5.0, 11.0, 1.0, 11.0),
            Block.box(4.0, 1.0, 4.0, 12.0, 9.0, 12.0),
            Block.box(6.0, 9.0, 6.0, 10.0, 14.0, 10.0),
            Block.box(5.0, 14.0, 5.0, 11.0, 16.0, 11.0)
    );

    private final Variant variant;

    public UrnBlock(BlockBehaviour.Properties properties, Variant variant) {
        super(properties);
        this.variant = variant;
        registerDefaultState(defaultBlockState().setValue(PAINTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PAINTED);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new UrnBlockEntity(blockPos, blockState);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return variant == Variant.URN ? URN_SHAPE : AMPHORE_SHAPE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return variant == Variant.URN ? URN_SHAPE : AMPHORE_SHAPE;
    }

    @Override
    public @NotNull VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Items.HONEYCOMB)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof UrnBlockEntity urnBlockEntity)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!world.isClientSide) {
            int baseRgb = urnBlockEntity.getSideColorRgb() != 0 ? urnBlockEntity.getSideColorRgb() : urnBlockEntity.getGlazeColorRgb();
            if (baseRgb != 0) {
                urnBlockEntity.setGlazed(true);
                urnBlockEntity.setGlazeColorRgb(baseRgb);

                float nextStrength = urnBlockEntity.getGlazeStrength() + 0.25f;
                if (nextStrength > 2.0f) {
                    nextStrength = 2.0f;
                }
                urnBlockEntity.setGlazeStrength(nextStrength);

                world.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
                world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }
        }

        return ItemInteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int earthy = 0xB08D57;
        int gold = 0xFFD700;

        tooltipComponents.add(Component.translatable("tooltip.lilis_pottery.canbeplaced")
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));

        boolean shiftDown = Screen.hasShiftDown();
        if (!shiftDown) {
            Component key = Component.literal("[SHIFT]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(gold)));
            tooltipComponents.add(Component.translatable("tooltip.lilis_pottery.tooltip_information.hold", key)
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
        }

        if (shiftDown) {
            tooltipComponents.add(Component.translatable("tooltip.lilis_pottery.tooltip_information.info_3")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
            tooltipComponents.add(Component.empty());
            tooltipComponents.add(Component.translatable("tooltip.lilis_pottery.tooltip_information.info_0")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
            tooltipComponents.add(Component.empty());
            tooltipComponents.add(Component.translatable("tooltip.lilis_pottery.tooltip_information.info_1")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            boolean painted = tag.contains("painted", 1) && tag.getBoolean("painted");
            int rgb = tag.contains("sideColorRgb", 99) ? tag.getInt("sideColorRgb") : 0;

            if (painted && rgb != 0) {
                tooltipComponents.add(Component.empty());

                DyeColor nearest = nearestDyeColor(rgb);
                Component colorName = Component.translatable("color.minecraft." + nearest.getName())
                        .withStyle(style -> style.withColor(rgb & 0xFFFFFF));

                tooltipComponents.add(Component.translatable("tooltip.lilis_pottery.painted", colorName)
                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
            }

            if (tag.contains("glazed", 1) && tag.getBoolean("glazed")) {
                tooltipComponents.add(Component.translatable("tooltip.lilis_pottery.glazed")
                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
            }
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide) {
            return;
        }

        int sideColorRgb = 0;
        boolean painted;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();

            if (tag.contains("sideColorRgb", 99)) {
                sideColorRgb = tag.getInt("sideColorRgb");
            }

            if (tag.contains("painted", 1)) {
                painted = tag.getBoolean("painted");
            } else {
                painted = sideColorRgb != 0;
            }

            if (state.hasProperty(PAINTED) && state.getValue(PAINTED) != painted) {
                level.setBlock(pos, state.setValue(PAINTED, painted), 2);
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof UrnBlockEntity urnBlockEntity) {
                urnBlockEntity.setSideColorRgb(sideColorRgb);
                urnBlockEntity.setPainted(painted);

                if (tag.contains("glazed", 1)) {
                    urnBlockEntity.setGlazed(tag.getBoolean("glazed"));
                }
                if (tag.contains("glazeColorRgb", 99)) {
                    urnBlockEntity.setGlazeColorRgb(tag.getInt("glazeColorRgb"));
                }
                if (tag.contains("glazeStrength", 99)) {
                    urnBlockEntity.setGlazeStrength(tag.getFloat("glazeStrength"));
                }
            }
        }
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (!(blockEntity instanceof UrnBlockEntity urnBlockEntity)) {
            return drops;
        }

        for (ItemStack drop : drops) {
            if (drop.is(this.asItem())) {
                CompoundTag tag = new CompoundTag();

                CustomData existing = drop.get(DataComponents.CUSTOM_DATA);
                if (existing != null) {
                    tag = existing.copyTag();
                }

                int sideColorRgb = urnBlockEntity.getSideColorRgb();
                boolean painted = urnBlockEntity.isPainted();

                if (sideColorRgb != 0) {
                    tag.putInt("sideColorRgb", sideColorRgb);
                }

                tag.putBoolean("painted", painted);

                if (urnBlockEntity.isGlazed()) {
                    tag.putBoolean("glazed", true);
                    int glazeColorRgb = urnBlockEntity.getGlazeColorRgb();
                    if (glazeColorRgb != 0) {
                        tag.putInt("glazeColorRgb", glazeColorRgb);
                    }
                    tag.putFloat("glazeStrength", urnBlockEntity.getGlazeStrength());
                }

                drop.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }

        return drops;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock() && level instanceof ServerLevel serverLevel) {
            BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
            if (blockEntity instanceof UrnBlockEntity urnBlockEntity) {
                urnBlockEntity.dropAll(serverLevel, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
        return state;
    }
}