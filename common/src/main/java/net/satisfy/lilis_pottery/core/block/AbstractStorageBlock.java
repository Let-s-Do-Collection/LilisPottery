package net.satisfy.lilis_pottery.core.block;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
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
import net.satisfy.lilis_pottery.core.block.entity.AbstractStorageBlockEntity;
import net.satisfy.lilis_pottery.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public abstract class AbstractStorageBlock extends AbstractFacingBlock implements EntityBlock {
    public static final BooleanProperty PAINTED = BooleanProperty.create("painted");

    public AbstractStorageBlock(BlockBehaviour.Properties settings) {
        super(settings);
        BlockState baseState = this.stateDefinition.any();
        if (baseState.hasProperty(PAINTED)) {
            this.registerDefaultState(baseState.setValue(PAINTED, false));
        } else {
            this.registerDefaultState(baseState);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PAINTED);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof AbstractStorageBlockEntity storageEntity)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.is(Items.HONEYCOMB)) {
            int baseRgb = storageEntity.getSideColorRgb();
            boolean canGlaze = baseRgb != 0 && storageEntity.getGlazeStrength() < 2.0f;

            if (!canGlaze) {
                return ItemInteractionResult.CONSUME;
            }

            if (!world.isClientSide) {
                storageEntity.setGlazed(true);
                storageEntity.setGlazeColorRgb(baseRgb);

                float nextStrength = storageEntity.getGlazeStrength() + 0.25f;
                if (nextStrength > 2.0f) {
                    nextStrength = 2.0f;
                }
                storageEntity.setGlazeStrength(nextStrength);

                world.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
                world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }

            return ItemInteractionResult.sidedSuccess(world.isClientSide);
        }

        Optional<Tuple<Float, Float>> hitCoordinates = GeneralUtil.getRelativeHitCoordinatesForBlockFace(
                hit,
                state.getValue(FACING),
                this.unAllowedDirections()
        );

        if (hitCoordinates.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        Tuple<Float, Float> coordinates = hitCoordinates.get();
        int section = this.getSection(coordinates.getA(), coordinates.getB());
        if (section == Integer.MIN_VALUE) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!storageEntity.getInventory().get(section).isEmpty()) {
            this.remove(world, pos, player, storageEntity, section);
            return ItemInteractionResult.sidedSuccess(world.isClientSide);
        }

        if (!stack.isEmpty() && this.canInsertStack(stack)) {
            this.add(world, pos, player, storageEntity, stack, section);
            return ItemInteractionResult.sidedSuccess(world.isClientSide);
        }

        return ItemInteractionResult.CONSUME;
    }

    public void add(Level level, BlockPos pos, Player player, AbstractStorageBlockEntity storageEntity, ItemStack itemStack, int index) {
        if (!level.isClientSide) {
            SoundEvent sound = SoundEvents.GRASS_PLACE;
            storageEntity.setStack(index, itemStack.split(1));
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (player.isCreative()) {
                itemStack.grow(1);
            }

            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
    }

    public void remove(Level level, BlockPos pos, Player player, AbstractStorageBlockEntity storageEntity, int index) {
        if (!level.isClientSide) {
            ItemStack removedStack = storageEntity.removeStack(index);
            SoundEvent sound = SoundEvents.GRASS_BREAK;
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getInventory().add(removedStack)) {
                player.drop(removedStack, false);
            }

            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
    }

    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AbstractStorageBlockEntity storageEntity) {
                if (world instanceof ServerLevel serverLevel) {
                    Containers.dropContents(serverLevel, pos, storageEntity.getInventory());
                }

                world.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, world, pos, newState, moved);
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public abstract int size();

    public abstract ResourceLocation type();

    public abstract Direction[] unAllowedDirections();

    public abstract boolean canInsertStack(ItemStack stack);

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AbstractStorageBlockEntity(pos, state, this.size());
    }

    public abstract int getSection(Float x, Float y);

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
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
            if (blockEntity instanceof AbstractStorageBlockEntity storageEntity) {
                storageEntity.setSideColorRgb(sideColorRgb);
                storageEntity.setPainted(painted);

                if (tag.contains("glazed", 1)) {
                    storageEntity.setGlazed(tag.getBoolean("glazed"));
                }
                if (tag.contains("glazeColorRgb", 99)) {
                    storageEntity.setGlazeColorRgb(tag.getInt("glazeColorRgb"));
                }
                if (tag.contains("glazeStrength", 99)) {
                    storageEntity.setGlazeStrength(tag.getFloat("glazeStrength"));
                }
            }
        }
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

    static DyeColor nearestDyeColor(int rgb) {
        DyeColor best = DyeColor.WHITE;
        int bestDistance = Integer.MAX_VALUE;

        for (DyeColor dyeColor : DyeColor.values()) {
            int dyeRgb = dyeColor.getFireworkColor();
            int distance = colorDistanceSquared(rgb, dyeRgb);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = dyeColor;
            }
        }

        return best;
    }

    private static int colorDistanceSquared(int firstRgb, int secondRgb) {
        int firstRed = (firstRgb >> 16) & 255;
        int firstGreen = (firstRgb >> 8) & 255;
        int firstBlue = firstRgb & 255;

        int secondRed = (secondRgb >> 16) & 255;
        int secondGreen = (secondRgb >> 8) & 255;
        int secondBlue = secondRgb & 255;

        int redDiff = firstRed - secondRed;
        int greenDiff = firstGreen - secondGreen;
        int blueDiff = firstBlue - secondBlue;

        return redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff;
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (!(blockEntity instanceof AbstractStorageBlockEntity storageEntity)) {
            return drops;
        }

        for (ItemStack drop : drops) {
            if (drop.is(this.asItem())) {
                applyPaintData(drop, storageEntity);
            }
        }

        return drops;
    }

    private void applyPaintData(ItemStack stack, AbstractStorageBlockEntity storageEntity) {
        CompoundTag tag = new CompoundTag();

        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        if (existing != null) {
            tag = existing.copyTag();
        }

        int sideColorRgb = storageEntity.getSideColorRgb();
        boolean painted = storageEntity.isPainted();

        if (sideColorRgb != 0) {
            tag.putInt("sideColorRgb", sideColorRgb);
        }

        tag.putBoolean("painted", painted);

        if (storageEntity.isGlazed()) {
            tag.putBoolean("glazed", true);
            int glazeColorRgb = storageEntity.getGlazeColorRgb();
            if (glazeColorRgb != 0) {
                tag.putInt("glazeColorRgb", glazeColorRgb);
            }
            tag.putFloat("glazeStrength", storageEntity.getGlazeStrength());
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}